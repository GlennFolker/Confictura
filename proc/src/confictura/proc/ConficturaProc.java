package confictura.proc;

import arc.*;
import arc.assets.*;
import arc.files.*;
import arc.func.*;
import arc.graphics.*;
import arc.mock.*;
import arc.struct.*;
import arc.util.*;
import arc.util.Log.*;
import confictura.*;
import confictura.proc.GenAtlas.*;
import confictura.proc.list.*;
import confictura.util.*;
import mindustry.async.*;
import mindustry.core.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;

import java.util.*;
import java.util.concurrent.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class ConficturaProc{
    protected static TaskQueue runs = new TaskQueue();
    public static GenAtlas atlas;

    public static ConficturaMod main;
    public static ModMeta meta;
    public static LoadedMod mod;

    protected static Fi assetsDir, outputDir;

    public static void main(String[] args){
        var exec = Threads.executor("Confictura-Executor", OS.cores * 2);
        assetsDir = Fi.get(args[0]);
        outputDir = Fi.get(args[1]);

        Log.logger = new NoopLogHandler();
        try{
            ArcNativesLoader.load();
        }catch(Throwable ignored){}

        headless = true;
        app = new MockApplication(){
            @Override
            public void post(Runnable runnable){
                runs.post(runnable);
            }
        };
        files = new MockFiles();
        assets = new AssetManager(tree = new FileTree(){
            @Override
            public Fi get(String path, boolean safe){
                var file = assetsDir.child(path);
                return file.exists() ? file : files.internal(path);
            }
        });
        settings = new Settings();
        Core.atlas = atlas = new GenAtlas();

        asyncCore = new AsyncCore();
        state = new GameState();
        mods = new Mods();

        content = new ContentLoader();
        content.createBaseContent();

        main = new ConficturaMod();
        meta = new ModMeta(){{name = "confictura";}};
        mod = new LoadedMod(null, null, main, ConficturaProc.class.getClassLoader(), meta);

        Events.fire(new FileTreeInitEvent());
        runs.run();

        content.setCurrentMod(mod);
        main.loadContent();
        content.setCurrentMod(null);
        runs.run();

        main.init();
        loadLogger();

        wait(f -> Fi.get("sprites").walk(file -> {
            if(file.extEquals("png")) f.get(exec.submit(() -> {
                var region = new GenRegion((file.path().contains("vanilla/") ? "" : "confictura-") + file.nameWithoutExtension(), file, new Pixmap(file));
                atlas.addRegion(region);
            }));
        }));

        wait(f -> BlockProc.init(run -> f.get(exec.submit(run))));

        wait(f -> atlas.each(reg -> f.get(exec.submit(() -> {
            var pix = reg.pixmap;
            Pixmaps.bleed(pix);

            var prev = pix.copy();
            Color color = new Color(), sum = new Color(), suma = new Color();

            int[] p = new int[9];
            for(int x = 0; x < prev.width; x++){
                for(int y = 0; y < prev.height; y++){
                    int A = prev.getRaw(Math.max(x - 1, 0), Math.min(y + 1, prev.height - 1)),
                        B = prev.getRaw(x, Math.min(y + 1, prev.height - 1)),
                        C = prev.getRaw(Math.min(x + 1, prev.width - 1), Math.min(y + 1, prev.height - 1)),
                        D = prev.getRaw(Math.max(x - 1, 0), y),
                        E = prev.getRaw(x, y),
                        F = prev.getRaw(Math.min(x + 1, prev.width - 1), y),
                        G = prev.getRaw(Math.max(x - 1, 0), Math.max(y - 1, 0)),
                        H = prev.getRaw(x, Math.max(y - 1, 0)),
                        I = prev.getRaw(Math.min(x + 1, prev.width - 1), Math.max(y - 1, 0));

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

                    float total = 0;
                    sum.set(0f, 0f, 0f, 0f);

                    for(int val : p){
                        color.set(val);
                        float a = color.a;
                        color.lerp(suma, (1f - a));
                        sum.r += color.r;
                        sum.g += color.g;
                        sum.b += color.b;
                        sum.a += a;
                        total += 1f;
                    }

                    fm = (1f / total);
                    sum.mul(fm, fm, fm, fm);
                    pix.setRaw(x, y, sum.rgba());
                    sum.set(0f, 0f, 0f, 0f);
                }
            }

            var name = reg.file.name();
            if(name.endsWith(".floor.png")){
                new GenRegion(
                    reg.name,
                    reg.file.sibling(name.substring(0, name.length() - ".floor.png".length()) + ".png"),
                    Pixmaps.crop(pix, 1, 1, pix.width - 2, pix.height - 2)
                ).save(true);
                reg.file.delete();
            }else{
                reg.save(false);
            }
        }))));

        atlas.dispose();
        Threads.await(exec);
    }

    protected static <T> Seq<T> wait(Cons<Cons<Future<? extends T>>> futures){
        Seq<Future<? extends T>> array = new Seq<>();
        futures.get(array::add);

        return array.map(AsyncUtils::get);
    }
}
