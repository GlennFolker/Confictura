package confictura.util;

import arc.func.*;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import static arc.Core.*;

/**
 * Provides multithreading utilities, primarily synchronizations from threads to the main thread for OpenGL purposes.
 * @author GlFolker
 */
@SuppressWarnings("unchecked")
public final class AsyncUtils{
    private AsyncUtils(){
        throw new AssertionError();
    }

    public static <T> T get(Future<T> future){
        try{
            return future.get();
        }catch(InterruptedException | ExecutionException e){
            throw new RuntimeException(e);
        }
    }

    public static void postWait(Runnable runSync){
        var flag = new Semaphore(0);
        app.post(() -> {
            try{
                runSync.run();
            }finally{
                flag.release();
            }
        });

        try{
            flag.acquire();
        }catch(InterruptedException e){
            throw new RuntimeException(e);
        }
    }

    public static <T> T postWait(Prov<T> runSync){
        var flag = new Semaphore(0);

        var out = new Object[1];
        app.post(() -> {
            try{
                out[0] = runSync.get();
            }finally{
                flag.release();
            }
        });

        try{
            flag.acquire();
        }catch(InterruptedException e){
            throw new RuntimeException(e);
        }

        return (T)out[0];
    }

    public static <T> T lock(Lock lock, Prov<T> prov){
        lock.lock();
        var out = prov.get();

        lock.unlock();
        return out;
    }

    public static void lock(Lock lock, Runnable run){
        lock.lock();
        run.run();
        lock.unlock();
    }

    public static <T> T read(ReadWriteLock lock, Prov<T> prov){
        return lock(lock.readLock(), prov);
    }

    public static void read(ReadWriteLock lock, Runnable run){
        lock(lock.readLock(), run);
    }

    public static <T> T write(ReadWriteLock lock, Prov<T> prov){
        return lock(lock.writeLock(), prov);
    }

    public static void write(ReadWriteLock lock, Runnable run){
        lock(lock.writeLock(), run);
    }
}
