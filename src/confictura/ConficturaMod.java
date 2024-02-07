package confictura;

import arc.*;
import arc.graphics.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import confictura.content.*;
import confictura.gen.*;
import confictura.graphics.*;
import confictura.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;

import java.io.*;
import java.util.regex.*;

import static arc.Core.*;
import static mindustry.Vars.*;

/**
 * Main entry point of the mod. Handles startup things like content loading, entity registering, and utility bindings.
 * @author GlennFolker
 */
@SuppressWarnings("unchecked")
public class ConficturaMod extends Mod{
    public static DevBuild dev;

    public static Seq<String> packages;
    public static Seq<Class<?>> classes;

    public static boolean glsl33;

    public ConficturaMod(){
        try{
            Class<? extends DevBuild> devImpl = (Class<? extends DevBuild>)Class.forName("confictura.DevBuildImpl", true, mods.mainLoader());
            dev = devImpl.getConstructor().newInstance();

            Log.info("[Confictura] Instantiated developer build.");
        }catch(ClassNotFoundException | NoClassDefFoundError e){
            dev = new DevBuild(){};
            Log.info("[Confictura] Instantiated user build.");
        }catch(Throwable e){
            Log.err("[Confictura] Failed instantiating developer build", Strings.getFinalCause(e));
        }

        Events.on(FileTreeInitEvent.class, e -> {
            try(var reader = tree.get("meta/confictura-classes.json").reader()){
                var meta = Jval.read(reader);
                packages = meta.get("packages").asArray().map(Jval::asString);
                classes = meta.get("classes").asArray().map(val -> {
                    var name = val.asString();
                    var type = ReflectUtils.findClass(name);
                    if(type == null) Log.warn("Class '@' not found.", name);
                    return type;
                });
            }catch(IOException ex){
                throw new RuntimeException(ex);
            }

            if(!headless) app.post(CShaders::load);
        });

        app.post(() -> {
            ScriptUtils.init();
            ScriptUtils.importDefaults(ScriptUtils.modScope);

            if(!headless && app.isDesktop()){
                var gl = Gl.getString(Gl.version);
                var glsl = Gl.getString(Gl.shadingLanguageVersion);

                Log.info("[Confictura] GLSL version: @", glsl);
                if(!gl.toLowerCase().contains("opengl es")){
                    try{
                        var pattern = Pattern.compile("(\\d)\\.(\\d{2})");
                        for(var version : glsl.split("\\s+")){
                            var matcher = pattern.matcher(version);
                            if(matcher.find()){
                                int major = Integer.parseInt(matcher.group(1)),
                                    minor = Integer.parseInt(matcher.group(2));

                                glsl33 = major > 3 || major == 3 && minor >= 30;
                                break;
                            }
                        }
                    }catch(Throwable t){
                        Log.err("Couldn't parse GLSL version", t);
                    }
                }
                Log.info("[Confictura] IEEE 754 standard in desktop GLSL: @.", glsl33 ? "Yes" : "No");
            }
        });
    }

    @Override
    public void init(){
        dev.init();
    }

    @Override
    public void loadContent(){
        EntityRegistry.register();
        CPlanets.load();
    }
}
