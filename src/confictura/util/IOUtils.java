package confictura.util;

import arc.util.*;

import java.io.*;

/**
 * Input-output utilities, providing very specific functions that aren't really commonly used, but often enough to
 * require me to write a class for it.
 * @author GlennFolker
 */
@SuppressWarnings("unchecked")
public final class IOUtils{
    private IOUtils(){
        throw new AssertionError();
    }

    public static void ioErr(UnsafeRunnable run, String message) throws IOException{
        try{
            run.run();
        }catch(Throwable t){
            if(t instanceof IOException err) throw err;
            throw new IOException(message, t);
        }
    }

    public static void ioUnchecked(IORunnable run){
        try{
            run.run();
        }catch(IOException e){
            // This deduces the generic type to be `RuntimeException`, which is actually not assignable from `IOException`.
            // However, type erasure will erase all static casts anyway.
            // The result is, the code fools the compiler into thinking it's throwing `RuntimeException` and not have its
            // method signature explicitly throw `IOException`, even though it actually does.
            // Such is the way of Java...
            thrower(e);
        }
    }

    private static <T extends Throwable> void thrower(Throwable err) throws T{
        throw (T)err;
    }

    public interface IORunnable{
        void run() throws IOException;
    }
}
