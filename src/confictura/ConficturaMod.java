package confictura;

import arc.*;
import arc.assets.*;
import arc.files.*;
import arc.freetype.*;
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
import confictura.ui.*;
import confictura.ui.dialogs.*;
import confictura.util.*;
import confictura.world.blocks.environment.*;
import gltfrenzy.loader.*;
import gltfrenzy.model.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;

import java.io.*;

import static arc.Core.*;
import static confictura.util.StructUtils.*;
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

    public static CinematicEditorDialog cinematicDialog;
    public static ScriptDialog scriptDialog;

    protected static LoadedMod mod;

    public ConficturaMod(){
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

        if(!headless){
            var fontSuffix = ".confictura.gen";
            assets.setLoader(FreeTypeFontGenerator.class, ".confictura.gen", new FreeTypeFontGeneratorLoader(tree){
                @Override
                public FreeTypeFontGenerator load(AssetManager assetManager, String fileName, Fi file, FreeTypeFontGeneratorParameters parameter){
                    return new FreeTypeFontGenerator(resolve(fileName.substring(0, fileName.length() - fontSuffix.length())));
                }
            });

            assets.setLoader(Font.class, "-confictura", new FreetypeFontLoader(tree){
                @Override
                public Font loadSync(AssetManager manager, String fileName, Fi file, FreeTypeFontLoaderParameter parameter){
                    if(parameter == null) throw new IllegalArgumentException("FreetypeFontParameter must be set in AssetManager#load to point at a TTF file!");
                    return manager
                        .get(parameter.fontFileName + fontSuffix, FreeTypeFontGenerator.class)
                        .generateFont(parameter.fontParameters);
                }

                @Override
                @SuppressWarnings("rawtypes")
                public Seq<AssetDescriptor> getDependencies(String fileName, Fi file, FreeTypeFontLoaderParameter parameter){
                    return Seq.with(new AssetDescriptor<>(parameter.fontFileName + fontSuffix, FreeTypeFontGenerator.class));
                }
            });

            assets.setLoader(Scenes3D.class, ".gltf", new Scenes3DLoader(tree, new GltfReader()));
            assets.setLoader(Scenes3D.class, ".glb", new Scenes3DLoader(tree, new GlbReader()));

            assets.setLoader(MeshSet.class, new MeshSetLoader(tree));
            assets.setLoader(Node.class, new NodeLoader(tree));

            cinematicEditor = new CinematicEditor();

            assets.load(new Loadable(){
                ObjectMap<Texture, Pixmap> pixmaps;

                @Override
                public void loadAsync(){
                    pixmaps = new ObjectMap<>();
                    var regions = content.blocks().flatMap(b -> {
                        if(b instanceof EdgeFloor floor){
                            return chain(iter(b.variantRegions), iter(b.editorVariantRegions()));
                        }else{
                            return empty();
                        }
                    });

                    regions.each(r -> pixmaps.put(r.texture, null));
                    AsyncUtils.postWait(() -> {
                        var buffer = new FrameBuffer(2, 2);
                        for(var texture : pixmaps.keys()){
                            buffer.resize(texture.width, texture.height);
                            buffer.begin();
                            Draw.blit(texture, Shaders.screenspace);

                            var pixels = ScreenUtils.getFrameBufferPixmap(0, 0, texture.width, texture.height);
                            buffer.end();

                            // Generally it's a bad idea to modify a collection while iterating over it.
                            // However in this case, the map's inner hash tables are structurally unchanged, so it should be fine.
                            pixmaps.put(texture, pixels);
                        }
                        buffer.dispose();
                    });

                    regions.each(r -> {
                        var page = pixmaps.get(r.texture);
                        int x = r.getX(), y = r.getY(), w = r.width, h = r.height;

                        page.draw(page, x, y + h - 1, w, 1, x, y - 1, w, 1, false);
                        page.draw(page, x, y, 1, h, x + w, y, 1, h, false);
                        page.draw(page, x, y, w, 1, x, y + h, w, 1, false);
                        page.draw(page, x + w - 1, y, 1, h, x - 1, y, 1, h, false);

                        page.setRaw(x - 1, y - 1, page.getRaw(x + w - 1, y + h - 1));
                        page.setRaw(x + w, y - 1, page.getRaw(x, y + h - 1));
                        page.setRaw(x + w, y + h, page.getRaw(x, y));
                        page.setRaw(x - 1, y + h, page.getRaw(x + w - 1, y));
                    });
                }

                @Override
                public void loadSync(){
                    for(var e : pixmaps){
                        var texture = e.key;
                        var page = e.value;

                        texture.bind();
                        Gl.texImage2D(Gl.texture2d, 0, Gl.rgba, page.width, page.height, 0, Gl.rgba, Gl.unsignedByte, page.pixels);
                        page.dispose();
                    }

                    pixmaps.clear();
                    pixmaps = null;
                }

                @Override
                public String getName(){
                    return EdgeFloor.class.getSimpleName();
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

            if(!headless){
                CFonts.load();
                CModels.load();
                app.post(CShaders::load);
            }
        });

        app.post(() -> mod = mods.getMod(ConficturaMod.class));
    }

    @Override
    public void init(){
        dev.init();
        if(!headless){
            CStyles.load();

            cinematicDialog = new CinematicEditorDialog();
            scriptDialog = new ScriptDialog();
        }
    }

    @Override
    public void loadContent(){
        EntityRegistry.register();

        ScriptUtils.init();
        cinematic = new Cinematic();

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

    public static boolean isConfictura(@Nullable Content content){
        return content != null && isConfictura(content.minfo.mod);
    }

    public static boolean isConfictura(@Nullable LoadedMod mod){
        return mod != null && mod == mod();
    }

    public static LoadedMod mod(){
        return mod;
    }
}
