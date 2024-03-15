package confictura.proc;

import arc.files.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import arc.util.*;
import confictura.proc.GenAtlas.*;
import confictura.util.*;

import java.util.concurrent.locks.*;

import static confictura.proc.ConficturaProc.*;

public class GenAtlas extends TextureAtlas implements Eachable<GenRegion>{
    protected ObjectMap<String, GenRegion> regions = new ObjectMap<>();
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    public GenAtlas(){}

    public void addRegion(GenRegion region){
        var old = AsyncUtils.write(lock, () -> regions.put(region.name, region));
        if(old != null && old.found()) old.pixmap.dispose();
    }

    @Override
    public GenRegion find(String name){
        var reg = AsyncUtils.read(lock, () -> regions.get(name));
        if(reg == null){
            var err = new GenRegion(name, null, null);
            AsyncUtils.write(lock, () -> regions.put(name, err));
            return err;
        }
        return reg;
    }

    @Override
    public GenRegion find(String name, TextureRegion def){
        return AsyncUtils.read(lock, () -> regions.get(name, conv(def)));
    }

    @Override
    public PixmapRegion getPixmap(AtlasRegion region){
        if(!(region instanceof GenRegion gen)) throw new IllegalArgumentException("Invalid region class.");
        return new PixmapRegion(gen.pixmap());
    }

    public GenRegion conv(TextureRegion region){
        if(!(region instanceof GenRegion gen)) throw new IllegalArgumentException("Invalid region class.");
        return gen;
    }

    @Override
    public void each(Cons<? super GenRegion> cons){
        AsyncUtils.read(lock, () -> {
            for(var reg : regions.values()){
                if(reg.found() && reg.file != null && !reg.file.path().contains("vanilla/")) cons.get(reg);
            }
        });
    }

    @Override
    public void dispose(){
        AsyncUtils.write(lock, () -> {
            for(var region : regions.values()){
                if(region.found()) region.pixmap.dispose();
            }
            regions.clear();
        });
    }

    @Override
    public AtlasRegion addRegion(String name, TextureRegion region){
        throw new AssertionError("Stub!");
    }

    @Override
    public AtlasRegion addRegion(String name, Texture texture, int x, int y, int width, int height){
        throw new AssertionError("Stub!");
    }

    public static class GenRegion extends AtlasRegion{
        protected final @Nullable Pixmap pixmap;
        public final @Nullable Fi file;

        public GenRegion(String name, @Nullable Fi file, @Nullable Pixmap pixmap){
            this.name = name;
            this.file = file;
            this.pixmap = pixmap;

            u = v = 0f;
            u2 = v2 = 1f;
            if(pixmap != null){
                width = pixmap.width;
                height = pixmap.height;
            }
        }

        @Override
        public boolean found(){
            return pixmap != null;
        }

        public Pixmap pixmap(){
            if(pixmap == null) throw new IllegalArgumentException("Region '" + name + "' not found.");
            return pixmap;
        }

        public void save(boolean add){
            if(file == null) throw new IllegalStateException("Missing file for region '" + name + "'.");

            file.writePng(pixmap());
            if(add) atlas.addRegion(this);
        }
    }
}
