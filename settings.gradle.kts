pluginManagement{
    repositories{
        gradlePluginPortal()
        maven("https://jitpack.io")
    }

    plugins{
        val entVersion: String by settings
        id("com.github.GlennFolker.EntityAnno") version(entVersion)
    }
}

if(JavaVersion.current().ordinal < JavaVersion.VERSION_17.ordinal){
    throw IllegalStateException("JDK 17 is a required minimum version. Yours: ${System.getProperty("java.version")}")
}

val modArtifact: String by settings
rootProject.name = modArtifact

include(":proc")

for(local in arrayOf("glTFrenzy", "EntityAnno")){
    val prop = "local.${local.lowercase()}"
    if(!extra.has(prop) || extra[prop].toString().toBoolean()){
        val dir = File(rootDir.parent, local)
        if(dir.exists()){
            logger.lifecycle("Compiling with local $local.")
            includeBuild(dir){
                dependencySubstitution{
                    all{
                        val mindustryBE: String by settings
                        val mindustryBEVersion: String by settings
                        val arcVersion: String by settings

                        (requested as? ModuleComponentSelector)?.let{
                            if(mindustryBE.toBooleanStrict() && it.group == "com.github.Anuken.Mindustry"){
                                useTarget("com.github.Anuken.MindustryJitpack:${it.module}:$mindustryBEVersion")
                            }else if(it.group == "com.github.Anuken.Arc"){
                                useTarget("com.github.Anuken.Arc:${it.module}:$arcVersion")
                            }else if(it.group.startsWith("com.github.GlennFolker")){
                                val group = it.group.substring("com.github.GlennFolker".length)
                                if(group.isEmpty() && it.module == local){
                                    useTarget(project(":"))
                                }else if(group.isNotEmpty() && group.substring(1) == local){
                                    if(it.module.endsWith(".gradle.plugin")){
                                        useTarget(project(":"))
                                    }else{
                                        useTarget(project(":${it.module}"))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
