package confictura.proc.list;

import arc.func.*;
import arc.util.*;
import confictura.proc.*;

import static confictura.ConficturaMod.*;
import static mindustry.Vars.*;

public class UnitProc implements Proc{
    @Override
    public void init(Cons<Runnable> async){
        var packer = new GenPacker();
        content.units().each(u -> isConfictura(u) && u.generateIcons, u -> async.get(() -> {
            try{
                u.init();
                u.loadIcon();
                u.load();

                u.createIcons(packer);
                u.load();
            }catch(Throwable t){
                Log.warn("Skipping '@': @", u.name.substring("confictura-".length()), Strings.getStackTrace(Strings.getFinalCause(t)));
            }
        }));
    }

    @Override
    public void finish(){}
}
