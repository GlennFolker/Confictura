package confictura;

import arc.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import confictura.content.*;
import confictura.gen.*;
import confictura.graphics.*;
import confictura.util.*;
import confictura.world.*;
import gltfrenzy.loader.*;
import gltfrenzy.model.*;
import mindustry.game.EventType.*;
import mindustry.io.*;
import mindustry.io.SaveFileReader.*;
import mindustry.mod.*;

import java.io.*;

import static arc.Core.*;
import static mindustry.Vars.*;

/**
 * Main entry point of the mod. Handles startup things like content loading, entity registering, and utility bindings.
 * @author GlennFolker
 */
@SuppressWarnings("unchecked")
public class ConficturaMod extends Mod implements CustomChunk{
    public static DevBuild dev;

    public static Seq<String> packages;
    public static Seq<Class<?>> classes;

    protected final ObjectMap<Chunk, byte[]> buffer = new ObjectMap<>();
    protected static ConficturaMod instance;

    public ConficturaMod(){
        instance = this;
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
            assets.setLoader(Node.class, new NodeLoader(tree));
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

        app.post(ScriptUtils::init);
        SaveVersion.addCustomChunk("confictura", this);
    }

    @Override
    public void init(){
        dev.init();
    }

    @Override
    public void loadContent(){
        EntityRegistry.register();
        ScriptUtils.importDefaults(ScriptUtils.scope);

        CPlanets.load();
        CSectorPresets.load();
    }

    @Override
    public void write(DataOutput stream) throws IOException{
        ObjectMap<Chunk, byte[]> chunks = new ObjectMap<>();
        var bytes = new ByteArrayOutputStream(4096);
        var out = new DataOutputStream(bytes);

        if(ScriptedSector.current != null){
            bytes.reset();
            ScriptedSector.current.write(out);

            chunks.put(Chunk.scriptedSector, bytes.toByteArray());
        }

        stream.writeShort(chunks.size);
        for(var e : chunks){
            stream.writeUTF(e.key.name());
            stream.writeInt(e.value.length);
            stream.write(e.value, 0, e.value.length);
        }
    }

    @Override
    public void read(DataInput stream) throws IOException{
        buffer.clear();
        for(int i = 0, len = stream.readUnsignedShort(); i < len; i++){
            var key = stream.readUTF();
            var value = new byte[stream.readInt()];
            stream.readFully(value, 0, value.length);

            try{
                buffer.put(Chunk.valueOf(key), value);
            }catch(IllegalArgumentException e){
                Log.warn("No such chunk '" + key + "', skipping.");
            }
        }
    }

    public static void read(Chunk chunk, IORunner<DataInput> runner) throws IOException{
        var bytes = instance.buffer.remove(chunk);
        if(bytes == null) return;

        runner.accept(new DataInputStream(new ByteArrayInputStream(bytes)));
    }

    public enum Chunk{
        scriptedSector
    }
}
