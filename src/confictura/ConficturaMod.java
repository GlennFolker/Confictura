package confictura;

import arc.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import confictura.content.*;
import confictura.gen.*;
import confictura.graphics.*;
import confictura.util.*;
import gltfrenzy.loader.*;
import gltfrenzy.model.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;

import java.io.*;

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

        if(!headless){
            assets.setLoader(Scenes3D.class, ".gltf", new Scenes3DLoader(tree, new GltfReader()));
            assets.setLoader(Scenes3D.class, ".glb", new Scenes3DLoader(tree, new GlbReader()));

            assets.setLoader(MeshSet.class, new MeshSetLoader(tree));
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

            if(!headless) app.post(() -> {
                CShaders.load();
                CModels.load();
            });
        });

        app.post(() -> {
            ScriptUtils.init();
            ScriptUtils.importDefaults(ScriptUtils.modScope);
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
