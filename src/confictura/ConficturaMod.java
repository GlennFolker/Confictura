package confictura;

import arc.*;
import arc.assets.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
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
import java.util.*;
import java.util.concurrent.*;

import static arc.Core.*;
import static mindustry.Vars.*;

/**
 * Main entry point of the mod. Handles startup things like content loading, entity registering, and utility bindings.
 * @author GlennFolker
 */
@SuppressWarnings("unchecked")
public class ConficturaMod extends Mod implements CustomChunk, Loadable{
    public static DevBuild dev;

    public static Seq<String> packages;
    public static Seq<Class<?>> classes;

    protected final ObjectMap<Chunk, byte[]> buffer = new ObjectMap<>();
    protected long antialiasStart;

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

            if(settings.getBool("confictura-antialias", true)){
                assets.load(this);
            }
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

        Events.on(ClientLoadEvent.class, e -> ui.settings.addCategory(
            "@confictura",
            atlas.drawable("confictura-settings-icon"),
            st -> st.checkPref("confictura-antialias", true, b -> settings.put("confictura-antialias", b))
        ));

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

        CBlocks.load();
        CPlanets.load();
        CSectorPresets.load();
    }

    @Override
    public void loadAsync(){
        antialiasStart = Time.millis();

        var uiTexture = atlas.find("whiteui").texture;
        AsyncUtils.postWait(() -> atlas.getRegions().each(reg -> reg.texture != uiTexture && reg.name.startsWith("confictura-"), atlas::getPixmap));

        ObjectMap<PixmapRegion, Future<Pixmap>> result = new ObjectMap<>();

        var exec = Threads.executor("Confictura-Antialias", OS.cores * 2);
        atlas.getRegions().each(reg -> {
            if(reg.texture != uiTexture && reg.name.startsWith("confictura-")){
                // Don't call `atlas.getPixmap()`, instead throw an exception and disable antialiasing.
                var pixmap = reg.pixmapRegion;
                if(pixmap == null){
                    settings.put("confictura-antialias", false);
                    throw new IllegalStateException("Pixmap disposed! An external code (most likely your mods) had erroneously disposed the texture atlas' pixmaps.\nPlease submit an issue report!");
                }

                result.put(pixmap, exec.submit(() -> {
                    Pixmap
                        src = pixmap.crop(-1, -1, pixmap.width + 2, pixmap.height + 2),
                        dst = new Pixmap(src.width, src.height);

                    Color
                        color = new Color(),
                        sum = new Color(),
                        suma = new Color();

                    int[] p = new int[9];
                    src.each((x, y) -> {
                        int A = src.getRaw(Math.max(x - 1, 0), Math.min(y + 1, src.height - 1)),
                            B = src.getRaw(x, Math.min(y + 1, src.height - 1)),
                            C = src.getRaw(Math.min(x + 1, src.width - 1), Math.min(y + 1, src.height - 1)),
                            D = src.getRaw(Math.max(x - 1, 0), y),
                            E = src.getRaw(x, y),
                            F = src.getRaw(Math.min(x + 1, src.width - 1), y),
                            G = src.getRaw(Math.max(x - 1, 0), Math.max(y - 1, 0)),
                            H = src.getRaw(x, Math.max(y - 1, 0)),
                            I = src.getRaw(Math.min(x + 1, src.width - 1), Math.max(y - 1, 0));

                        Arrays.fill(p, E);
                        if(D == B && D != H && B != F) p[0] = D;
                        if((D == B && D != H && B != F && E != C) || (B == F && B != D && F != H && E != A)) p[1] = B;
                        if(B == F && B != D && F != H) p[2] = F;
                        if((H == D && H != F && D != B && E != A) || (D == B && D != H && B != F && E != G)) p[3] = D;
                        if((B == F && B != D && F != H && E != I) || (F == H && F != B && H != D && E != C)) p[5] = F;
                        if(H == D && H != F && D != B) p[6] = D;
                        if((F == H && F != B && H != D && E != G) || (H == D && H != F && D != B && E != I)) p[7] = H;
                        if(F == H && F != B && H != D) p[8] = F;

                        suma.set(0f, 0f, 0f, 0f);
                        for(int val : p){
                            color.set(val);
                            color.premultiplyAlpha();

                            suma.r += color.r;
                            suma.g += color.g;
                            suma.b += color.b;
                            suma.a += color.a;
                        }

                        float fm = suma.a <= 0.001f ? 0f : (1f / suma.a);
                        suma.mul(fm, fm, fm, fm);

                        float total = 0f;
                        sum.set(0f, 0f, 0f, 0f);

                        for(int val : p){
                            color.set(val);
                            float a = color.a;
                            color.lerp(suma, 1f - a);
                            sum.r += color.r;
                            sum.g += color.g;
                            sum.b += color.b;
                            sum.a += a;
                            total += 1f;
                        }

                        fm = 1f / total;
                        sum.mul(fm, fm, fm, fm);

                        dst.setRaw(x, y, sum.rgba());
                        sum.set(0f, 0f, 0f, 0f);
                    });

                    src.dispose();
                    return dst;
                }));
            }
        });

        var it = result.iterator();
        while(it.hasNext()){
            while(it.hasNext()){
                var next = it.next();
                if(next.value.isDone()){
                    var reg = next.key;
                    var out = AsyncUtils.get(next.value);

                    reg.pixmap.draw(out, reg.x - 1, reg.y - 1, reg.width + 1, reg.height + 1, false);
                    it.remove();
                }
            }

            it.reset();
        }

        Threads.await(exec);
    }

    @Override
    public void loadSync(){
        ObjectSet<Pixmap> dispose = new ObjectSet<>();
        atlas.getPixmaps().each((texture, pixmap) -> {
            TextureData prev = texture.getTextureData();
            if(prev instanceof PixmapTextureData pix){
                dispose.add(Reflect.get(PixmapTextureData.class, pix, "pixmap"));
            }else if(prev instanceof FileTextureData file){
                var cache = Reflect.<Pixmap>get(FileTextureData.class, file, "pixmap");
                if(cache != null && cache.isDisposed()) dispose.add(cache);
            }else{
                Log.warn("[Confictura] Unsupported texture data: `@`. Not disposing; potential memory leak may occur.", prev.getClass().getName());
            }

            texture.load(new PixmapTextureData(pixmap, false, false));
        });

        atlas.getTextures().each(atlas::disposePixmap);
        atlas.getRegions().each(reg -> reg.pixmapRegion = null);
        dispose.each(pix -> {
            if(!pix.isDisposed()) pix.dispose();
        });

        Log.info("[Confictura] Antialiasing sprites took @ms.", Time.timeSinceMillis(antialiasStart));
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
