package confictura;

import arc.*;
import arc.assets.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import confictura.cinematic.*;
import confictura.content.*;
import confictura.editor.*;
import confictura.gen.*;
import confictura.graphics.*;
import confictura.util.*;
import confictura.world.blocks.environment.*;
import gltfrenzy.loader.*;
import gltfrenzy.model.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;

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

    public static Seq<String> packages = Seq.with("java.lang", "java.util");
    public static Seq<Class<?>> classes = new Seq<>();
    public static ObjectIntMap<String> blockColors = new ObjectIntMap<>();

    public static Cinematic cinematic;

    public static CinematicEditor cinematicEditor;

    protected static ConficturaMod instance;

    public ConficturaMod(){
        instance = this;
        try{
            var devImpl = (Class<? extends DevBuild>)Class.forName("confictura.DevBuildImpl", true, mods.mainLoader());
            dev = devImpl.getConstructor().newInstance();

            Log.info("[Confictura] Instantiated developer build.");
        }catch(ClassNotFoundException | NoClassDefFoundError e){
            dev = new DevBuild(){};
            Log.info("[Confictura] Instantiated user build.");
        }catch(Throwable e){
            Log.err("[Confictura] Failed instantiating developer build", Strings.getFinalCause(e));
        }

        cinematic = new Cinematic();

        if(!headless){
            assets.setLoader(Scenes3D.class, ".gltf", new Scenes3DLoader(tree, new GltfReader()));
            assets.setLoader(Scenes3D.class, ".glb", new Scenes3DLoader(tree, new GlbReader()));

            assets.setLoader(MeshSet.class, new MeshSetLoader(tree));
            assets.setLoader(Node.class, new NodeLoader(tree));

            cinematicEditor = new CinematicEditor();

            assets.load(new Loadable(){
                Texture texture;
                Pixmap page;

                @Override
                public void loadAsync(){
                    page = AsyncUtils.postWait(() -> {
                        texture = atlas.find("stone1").texture;

                        var buffer = new FrameBuffer(texture.width, texture.height);
                        buffer.begin();
                        Draw.blit(texture, Shaders.screenspace);

                        var pixels = ScreenUtils.getFrameBufferPixmap(0, 0, texture.width, texture.height);
                        buffer.end();
                        buffer.dispose();

                        return pixels;
                    });

                    content.blocks().each(b -> b instanceof CFloor, (CFloor b) -> {
                        for(var r : b.variantRegions){
                            int x = r.getX(), y = r.getY(), w = r.width, h = r.height;
                            page.draw(page, x, y + h - 1, w, 1, x, y - 1, w, 1, false);
                            page.draw(page, x, y, 1, h, x + w, y, 1, h, false);
                            page.draw(page, x, y, w, 1, x, y + h, w, 1, false);
                            page.draw(page, x + w - 1, y, 1, h, x - 1, y, 1, h, false);

                            page.setRaw(x - 1, y - 1, page.getRaw(x + w - 1, y + h - 1));
                            page.setRaw(x + w, y - 1, page.getRaw(x, y + h - 1));
                            page.setRaw(x + w, y + h, page.getRaw(x, y));
                            page.setRaw(x - 1, y + h, page.getRaw(x + w - 1, y));
                        }
                    });
                }

                @Override
                public void loadSync(){
                    texture.bind();
                    Gl.texImage2D(Gl.texture2d, 0, Gl.rgba, page.width, page.height, 0, Gl.rgba, Gl.unsignedByte, page.pixels);

                    page.dispose();
                }

                @Override
                public String getName(){
                    return "confictura-floor-refine";
                }
            });
        }

        Events.on(FileTreeInitEvent.class, e -> {
            try(var reader = tree.get("meta/confictura/classes.json").reader()){
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

            var colors = tree.get("meta/confictura/block-colors.json");
            if(colors.exists()){
                try(var reader = colors.reader()){
                    var meta = Jval.read(reader);
                    for(var child : meta.asObject()){
                        blockColors.put(child.key, child.value.asInt());
                    }
                }catch(IOException ex){
                    throw new RuntimeException(ex);
                }
            }

            if(!headless) app.post(() -> {
                CShaders.load();
                CModels.load();
            });
        });

        app.post(ScriptUtils::init);
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

        for(var e : blockColors){
            var block = content.block("confictura-" + e.key);
            if(block == null) throw new IllegalStateException("Block 'confictura-" + e.key + "' not found; try re-running `proc:run`.");

            block.mapColor.set(e.value);
            block.squareSprite = block.mapColor.a > 0.5f;
            block.mapColor.a = 1f;
            block.hasColor = true;
        }
    }

    public static boolean isConfictura(LoadedMod mod){
        return mod != null && mod.main == instance;
    }
}
