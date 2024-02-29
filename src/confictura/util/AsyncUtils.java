package confictura.util;

import arc.func.*;

import java.util.concurrent.*;

import static arc.Core.*;

/**
 * Provides multithreading utilities, primarily synchronizations from threads to the main thread for OpenGL purposes.
 * @author GlennFolker
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
            runSync.run();
            flag.release();
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
            out[0] = runSync.get();
            flag.release();
        });

        try{
            flag.acquire();
        }catch(InterruptedException e){
            throw new RuntimeException(e);
        }

        return (T)out[0];
    }
}
