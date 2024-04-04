package confictura.proc.list;

import arc.func.*;
import confictura.graphics.g2d.*;
import confictura.proc.*;
import confictura.proc.GenAtlas.*;

import static confictura.proc.ConficturaProc.*;

public class EffectProc implements Proc{
    @Override
    public void init(Cons<Runnable> async){
        async.get(() -> {
            var brand = atlas.find("confictura-monolith-brand");
            new GenRegion(
                "confictura-monolith-brand-glow",
                brand.file.sibling("monolith-brand-glow.png"),
                CPixmaps.gaussianBlur(brand.pixmap(), 16, 16, 4f)
            ).save(true);
        });
    }

    @Override
    public void finish(){}
}
