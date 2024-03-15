package confictura.proc;

import arc.func.*;

public interface Proc{
    void init(Cons<Runnable> async);

    void finish();
}
