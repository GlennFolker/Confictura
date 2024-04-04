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
import java.util.concurrent.atomic.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class ConficturaProc{
    protected static final TaskQueue runs = new TaskQueue();
    protected static final Proc[] procs = {new BlockProc(), new EffectProc()};

    public static GenAtlas atlas;

    public static ConficturaMod main;
    public static ModMeta meta;
    public static LoadedMod mod;

    public static Fi assetsDir;

    public static void main(String[] args){
        loadLogger();
        Log.info("[Confictura-Proc] Sprite processing initiated...");

        var logger = Log.logger;
        Log.logger = new NoopLogHandler();

        long mark = Time.millis(), program = mark;

        var exec = Threads.executor("Confictura-Executor", OS.cores * 2);
        assetsDir = Fi.get(args[0]);

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

        Reflect.<Seq<LoadedMod>>get(Mods.class, mods, "mods").add(mod);
        Reflect.<ObjectMap<Class<?>, ModMeta>>get(Mods.class, mods, "metas").put(ConficturaMod.class, meta);

        Events.fire(new FileTreeInitEvent());
        runs.run();

        content.setCurrentMod(mod);
        main.loadContent();
        content.setCurrentMod(null);
        runs.run();

        main.init();
        Log.logger = logger;

        Log.info("[Confictura-Proc] Initialization took @ms.", Time.timeSinceMillis(mark));
        mark = Time.millis();

        wait(f -> Fi.get("sprites").walk(file -> {
            if(file.extEquals("png")) f.get(exec.submit(() -> {
                var region = new GenRegion((file.path().contains("vanilla/") ? "" : "confictura-") + file.nameWithoutExtension(), file, new Pixmap(file));
                atlas.addRegion(region);
            }));
        }));

        Log.info("[Confictura-Proc] Pixmaps atlas mapping took @ms.", Time.timeSinceMillis(mark));
        wait(f -> {
            for(var proc : procs){
                long started = Time.millis();

                Seq<Runnable> runs = new Seq<>();
                proc.init(runs::add);

                var remaining = new AtomicInteger(runs.size);
                runs.each(run -> f.get(exec.submit(() -> {
                    run.run();
                    if(remaining.addAndGet(-1) == 0){
                        proc.finish();
                        Log.info("[Confictura-Proc] '@' took @ms.", proc.getClass().getSimpleName(), Time.timeSinceMillis(started));
                    }
                })));
            }
        });

        mark = Time.millis();
        wait(f -> atlas.each(reg -> f.get(exec.submit(() -> {
            var pix = reg.pixmap;
            Pixmaps.bleed(pix, Integer.MAX_VALUE);

            var prev = pix.copy();
            Color color = new Color(), sum = new Color(), suma = new Color();

            int[] samples = new int[9];
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

                    Arrays.fill(samples, E);
                    if(D == B && D != H && B != F) samples[0] = D;
                    if((D == B && D != H && B != F && E != C) || (B == F && B != D && F != H && E != A)) samples[1] = B;
                    if(B == F && B != D && F != H) samples[2] = F;
                    if((H == D && H != F && D != B && E != A) || (D == B && D != H && B != F && E != G)) samples[3] = D;
                    if((B == F && B != D && F != H && E != I) || (F == H && F != B && H != D && E != C)) samples[5] = F;
                    if(H == D && H != F && D != B) samples[6] = D;
                    if((F == H && F != B && H != D && E != G) || (H == D && H != F && D != B && E != I)) samples[7] = H;
                    if(F == H && F != B && H != D) samples[8] = F;

                    suma.set(0f, 0f, 0f, 0f);
                    for(int val : samples){
                        color.set(val).premultiplyAlpha();
                        suma.r += color.r;
                        suma.g += color.g;
                        suma.b += color.b;
                        suma.a += color.a;
                    }

                    float fm = suma.a <= 0.001f ? 0f : (1f / suma.a);
                    suma.mul(fm, fm, fm, fm);

                    float total = 0;
                    sum.set(0f, 0f, 0f, 0f);

                    for(int val : samples){
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
                }
            }

            prev.dispose();
            Pixmaps.bleed(pix, Integer.MAX_VALUE);

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

        Log.info("[Confictura-Proc] Anti-aliasing and alpha-bleeding took @ms.", Time.timeSinceMillis(mark));

        atlas.dispose();
        Threads.await(exec);

        Log.info("[Confictura-Proc] Sprite processing finished, took @ms.", Time.timeSinceMillis(program));
    }

    protected static <T> void wait(Cons<Cons<Future<? extends T>>> futures){
        Seq<Future<? extends T>> array = new Seq<>();
        futures.get(array::add);

        array.each(AsyncUtils::get);
    }
}
