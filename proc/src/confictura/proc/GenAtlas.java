package confictura.proc;

import arc.files.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import arc.util.*;
import confictura.proc.GenAtlas.*;

import java.util.concurrent.locks.*;

import static confictura.proc.ConficturaProc.*;

public class GenAtlas extends TextureAtlas implements Eachable<GenRegion>{
    protected ObjectMap<String, GenRegion> regions = new ObjectMap<>();
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    public GenAtlas(){}

    private <T> T read(Prov<T> prov){
        var read = lock.readLock();
        read.lock();

        var out = prov.get();

        read.unlock();
        return out;
    }

    private void read(Runnable run){
        var read = lock.readLock();
        read.lock();

        run.run();
        read.unlock();
    }

    private <T> T write(Prov<T> prov){
        var write = lock.writeLock();
        write.lock();

        var out = prov.get();

        write.unlock();
        return out;
    }

    private void write(Runnable run){
        var write = lock.writeLock();
        write.lock();

        run.run();
        write.unlock();
    }

    public GenRegion addRegion(GenRegion region){
        var old = write(() -> regions.put(region.name, region));

        if(old != null) old.pixmap.dispose();
        return region;
    }

    @Override
    public GenRegion find(String name){
        var reg = read(() -> regions.get(name));
        if(reg == null){
            var err = new GenRegion(name, null, null);
            write(() -> regions.put(name, err));
            return err;
        }
        return reg;
    }

    @Override
    public GenRegion find(String name, TextureRegion def){
        return read(() -> regions.get(name, conv(def)));
    }

    public GenRegion conv(TextureRegion region){
        if(!(region instanceof GenRegion gen)) throw new IllegalArgumentException("Invalid region class.");
        return gen;
    }

    @Override
    public void each(Cons<? super GenRegion> cons){
        for(var reg : regions.values()){
            if(reg.found()) cons.get(reg);
        }
    }

    @Override
    public void dispose(){
        write(() -> {
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

            file.writePng(atlas.conv(this).pixmap());
            if(add) atlas.addRegion(this);
        }
    }
}
