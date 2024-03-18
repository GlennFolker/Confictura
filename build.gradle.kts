import arc.files.*
import arc.struct.*
import arc.util.*
import arc.util.io.*
import arc.util.serialization.*
import ent.*
import java.io.*
import java.nio.charset.*
import java.util.regex.*

buildscript{
    dependencies{
        val arcVersion: String by project
        classpath("com.github.Anuken.Arc:arc-core:$arcVersion")
    }

    repositories{
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/releases/")
        maven("https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository")
        maven("https://jitpack.io")
    }
}

plugins{
    // Register `EntityAnno` plugin, but only apply on `rootProject`.
    `java-library`
    id("com.github.GlennFolker.EntityAnno") apply(false)
}

configure<ExtraPropertiesExtension>{
    val props = StringMap()

    val local = layout.projectDirectory.file("local.properties").asFile
    if(local.exists()){
        logger.lifecycle("Found `local.properties` file.")
        FileReader(local, StandardCharsets.UTF_8).use{
            PropertiesUtils.load(props, it)
        }
    }

    props.each{key, value -> set(key, value)}
}

val arcVersion: String by project
val mindustryVersion: String by project
val mindustryBEVersion: String by project
val glTFrenzyVersion: String by project
val entVersion: String by project

val modName: String by project
val modArtifact: String by project
val modFetch: String by project
val modGenSrc: String by project
val modGen: String by project

val androidSdkVersion: String by project
val androidBuildVersion: String by project
val androidMinVersion: String by project

val useJitpack = property("mindustryBE").toString().toBooleanStrict()
val isDev = hasProperty("mod.dev") && property("mod.dev").toString().toBooleanStrict()

fun arc(module: String): String{
    return "com.github.Anuken.Arc$module:$arcVersion"
}

fun mindustry(module: String): String{
    return "com.github.Anuken.Mindustry$module:$mindustryVersion"
}

fun glTFrenzy(): String{
    return "com.github.GlennFolker:glTFrenzy:$glTFrenzyVersion"
}

fun entity(module: String): String{
    return "com.github.GlennFolker.EntityAnno$module:$entVersion"
}

allprojects{
    apply(plugin = "java-library")
    sourceSets["main"].java.setSrcDirs(arrayListOf(layout.projectDirectory.dir("src")))

    configurations.configureEach{
        // Resolve the correct Mindustry dependency, and force Arc version.
        resolutionStrategy.eachDependency{
            if(useJitpack && requested.group == "com.github.Anuken.Mindustry"){
                useTarget("com.github.Anuken.MindustryJitpack:${requested.module.name}:$mindustryBEVersion")
            }else if(requested.group == "com.github.Anuken.Arc"){
                useVersion(arcVersion)
            }
        }
    }

    dependencies{
        // Downgrade Java 9+ syntax into being available in Java 8.
        annotationProcessor(entity(":downgrader"))
    }

    repositories{
        // Necessary Maven repositories to pull dependencies from.
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/releases/")

        // Use Zelaux's non-buggy repository for release Mindustry and Arc builds.
        if(!useJitpack) maven("https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository")
        maven("https://jitpack.io")
    }

    tasks.withType<JavaCompile>().configureEach{
        // Use Java 17+ syntax, but target Java 8 bytecode version.
        sourceCompatibility = "17"
        options.apply{
            release = 8
            compilerArgs.add("-Xlint:-options")

            isIncremental = true
            encoding = "UTF-8"
        }
    }
}

project(":proc"){
    dependencies{
        implementation(rootProject)

        implementation(mindustry(":core"))
        implementation(arc(":arc-core"))
        implementation(arc(":natives-desktop"))
    }

    val fetchFiles = tasks.register<DefaultTask>("fetchFiles"){
        val out = layout.buildDirectory.dir("fetched")
        val cache = out.map{it.file("cache.txt")}

        outputs.dir(out)
        outputs.upToDateWhen{
            val c = cache.get().asFile
            c.exists() && c.readText(StandardCharsets.UTF_8) == if(useJitpack) mindustryBEVersion else mindustryVersion
        }

        doFirst{
            val dir = out.get().asFile
            dir.deleteRecursively()
            dir.mkdirs()

            val exec = Threads.executor("Confictura-Fetcher", OS.cores)
            arrayOf<Pair<String, String?>>(
                Pair("assets/scripts/global.js", "scripts/global.js"),
                Pair("assets-raw/sprites/blocks/environment/edge-stencil.png", "sprites/vanilla/edge-stencil.png"),
            ).forEach{exec.submit{
                val (src, dst) = it
                Http.get("https://raw.githubusercontent.com/Anuken/${if(useJitpack) "MindustryJitpack" else "Mindustry"}/${if(useJitpack) mindustryBEVersion else mindustryVersion}/core/$src")
                    .timeout(0)
                    .error{logger.error("Couldn't fetch '$src'", it)}
                    .block{res ->
                        val target = File(dir, dst ?: src)
                        target.parentFile.mkdirs()
                        BufferedOutputStream(FileOutputStream(target)).use{Streams.copy(res.resultAsStream, it)}
                    }
            }}

            Threads.await(exec)

            val c = cache.get().asFile
            c.writeText(if(useJitpack) mindustryBEVersion else mindustryVersion, StandardCharsets.UTF_8)
        }
    }

    tasks.register<JavaExec>("run"){
        val assets = rootProject.layout.projectDirectory.dir("assets")
        val out = assets.dir("sprites")
        val colors = assets.dir("meta").dir("confictura").file("block-colors.json")
        val raw = assets.dir("sprites-raw")

        inputs.files(raw, fetchFiles)
        outputs.dir(out)
        outputs.file(colors)

        mainClass = "confictura.proc.ConficturaProc"
        classpath = sourceSets["main"].runtimeClasspath
        workingDir = temporaryDir
        standardInput = System.`in`
        args(assets.asFile)

        doFirst{
            val dir = out.asFile
            dir.deleteRecursively()
            dir.mkdirs()

            colors.asFile.delete()

            temporaryDir.deleteRecursively()
            temporaryDir.mkdirs()

            copy{
                from(files(raw))
                into(File(temporaryDir, "sprites"))
            }

            copy{
                from(files(fetchFiles))
                into(temporaryDir)
                exclude("cache.txt")
            }
        }

        doLast{
            copy{
                from(files(File(temporaryDir, "sprites")))
                into(out.asFile)
                exclude("vanilla/**")
            }
        }
    }
}

project(":"){
    apply(plugin = "com.github.GlennFolker.EntityAnno")

    configure<EntityAnnoExtension>{
        modName = project.properties["modName"].toString()
        mindustryVersion = project.properties[if(useJitpack) "mindustryBEVersion" else "mindustryVersion"].toString()
        isJitpack = useJitpack
        revisionDir = layout.projectDirectory.dir("revisions").asFile
        fetchPackage = modFetch
        genSrcPackage = modGenSrc
        genPackage = modGen
    }

    dependencies{
        // Use the entity generation annotation processor.
        compileOnly(entity(":entity"))
        add("kapt", entity(":entity"))

        compileOnly(mindustry(":core"))
        compileOnly(arc(":arc-core"))

        api(glTFrenzy())
    }

    val list = tasks.register<DefaultTask>("list"){
        inputs.files(tasks.named<JavaCompile>("compileJava"), configurations.runtimeClasspath)

        val output = layout.projectDirectory.dir("assets").dir("meta").dir("confictura").file("classes.json").asFile
        outputs.file(output)

        doFirst{
            output.parentFile.mkdirs()
            val packages = Jval.newArray()
            val classes = Jval.newArray()

            val forbid = Pattern.compile("\\$\\d+|.+Impl")
            fun proc(path: String, dir: File){
                dir.listFiles()?.forEach{
                    if(it.isDirectory && (path.startsWith("confictura") || it.name == "confictura")){
                        val visited = if(path.isEmpty()) it.name else "$path.${it.name}"
                        if(visited != modFetch && visited != modGenSrc){
                            packages.add(visited)
                            proc(visited, it)
                        }
                    }else{
                        val dot = it.name.lastIndexOf('.')
                        if(dot != -1){
                            val name = it.name.substring(0, dot)
                            val ext = it.name.substring(dot + 1)

                            if(ext == "class" && !forbid.matcher(name).matches()) classes.add("$path.$name")
                        }
                    }
                }
            }

            sourceSets["main"].runtimeClasspath.forEach{
                if(it.isDirectory){
                    proc("", it)
                }else if(it.exists()){
                    zipTree(it).forEach{inner -> proc("", inner)}
                }
            }

            val compacted = Jval.newObject().put("packages", packages).put("classes", classes)
            BufferedWriter(FileWriter(output, StandardCharsets.UTF_8, false)).use{compacted.writeTo(it, Jval.Jformat.formatted)}
        }
    }

    tasks.named<Delete>("clean"){
        val assets = layout.projectDirectory.dir("assets")
        delete(assets.dir("meta"))
        delete(assets.dir("sprites"))
    }

    tasks.named<Jar>("jar"){
        inputs.files(list)
        archiveFileName = "base.jar"
    }

    val deploy = tasks.register<Jar>("deploy"){
        val proc = project(":proc").tasks.named<JavaExec>("run")

        inputs.files(list)
        mustRunAfter(proc)

        if(!layout.projectDirectory.dir("assets").dir("sprites").asFile.exists()){
            logger.lifecycle("Sprites folder not found; automatically running `:proc:run`.")
            inputs.files(proc)
        }

        archiveFileName = "${modArtifact}Desktop.jar"

        from(files(sourceSets["main"].output.classesDirs))
        from(files(sourceSets["main"].output.resourcesDir))
        from(configurations.runtimeClasspath.map{conf -> conf.map{if(it.isDirectory) it else zipTree(it)}})

        from(files(layout.projectDirectory.dir("assets")){exclude("sprites-raw/**")})
        from(layout.projectDirectory.file("icon.png"))

        metaInf.from(layout.projectDirectory.file("LICENSE"))

        val meta = File(temporaryDir, "mod.json")
        from(meta)

        if(!isDev) exclude("**/**Impl*")
        doFirst{
            logger.lifecycle("Building ${if(isDev) "developer" else "user"} artifact.")

            val map = FileReader(layout.projectDirectory.file("mod.json").asFile, StandardCharsets.UTF_8)
                .use{Jval.read(it)}
                .put("name", modName)

            BufferedWriter(FileWriter(meta, StandardCharsets.UTF_8, false))
                .use{map.writeTo(it, Jval.Jformat.formatted)}
        }
    }

    tasks.register<Jar>("dex"){
        inputs.files(deploy)
        archiveFileName = "$modArtifact.jar"

        val desktopJar = deploy.flatMap{it.archiveFile}
        val dexJar = File(temporaryDir, "Dex.jar")

        from(zipTree(desktopJar), zipTree(dexJar))
        doFirst{
            exec{
                // Find Android SDK root.
                val sdkRoot = File(
                    OS.env("ANDROID_SDK_ROOT") ?: OS.env("ANDROID_HOME") ?:
                    throw IllegalStateException("Neither `ANDROID_SDK_ROOT` nor `ANDROID_HOME` is set.")
                )

                // Find `d8`.
                val d8 = File(sdkRoot, "build-tools/$androidBuildVersion/${if(OS.isWindows) "d8.bat" else "d8"}")
                if(!d8.exists()) throw IllegalStateException("Android SDK `build-tools;$androidBuildVersion` isn't installed or is corrupted")

                // Initialize a release build.
                val input = desktopJar.get().asFile
                val command = arrayListOf("$d8", "--release", "--min-api", androidMinVersion, "--output", "$dexJar", "$input")

                // Include all compile and runtime classpath.
                (configurations.compileClasspath.get().toList() + configurations.runtimeClasspath.get().toList()).forEach{
                    if(it.exists()) command.addAll(arrayOf("--classpath", it.path))
                }

                // Include Android platform as library.
                val androidJar = File(sdkRoot, "platforms/android-$androidSdkVersion/android.jar")
                if(!androidJar.exists()) throw IllegalStateException("Android SDK `platforms;android-$androidSdkVersion` isn't installed or is corrupted")

                command.addAll(arrayOf("--lib", "$androidJar"))
                if(OS.isWindows) command.addAll(0, arrayOf("cmd", "/c").toList())

                // Run `d8`.
                logger.lifecycle("Running `d8`.")
                commandLine(command)
            }
        }
    }

    tasks.register<DefaultTask>("install"){
        inputs.files(deploy)

        val desktopJar = deploy.flatMap{it.archiveFile}
        doLast{
            val input = desktopJar.get().asFile

            val folder = Fi.get(OS.getAppDataDirectoryString("Mindustry")).child("mods")
            folder.mkdirs()

            folder.child(input.name).delete()
            Fi(input).copyTo(folder)

            logger.lifecycle("Copied :deploy output to $folder.")
        }
    }
}
