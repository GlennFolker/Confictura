package confictura.proc.list;

import arc.func.*;
import arc.graphics.*;
import confictura.proc.GenAtlas.*;
import mindustry.world.blocks.environment.*;

import static confictura.proc.ConficturaProc.*;
import static mindustry.Vars.*;

public final class BlockProc{
    private BlockProc(){
        throw new AssertionError();
    }

    public static void init(Cons<Runnable> async){
        content.blocks().each(block -> block.minfo.mod == mod, block -> async.get(() -> {
            block.init();
            block.load();

            if(block instanceof Floor floor){
                for(var variant : floor.variantRegions){
                    var reg = atlas.conv(variant);
                    var pix = reg.pixmap();

                    var out = new Pixmap(pix.width + 2, pix.height + 2);
                    // Draw center.
                    out.draw(pix, 1, 1);
                    // Draw opposite edges.
                    out.draw(pix, 1, 0, 0, pix.height - 1, pix.width, 1);
                    out.draw(pix, 1, out.height - 1, 0, 0, pix.width, 1);
                    out.draw(pix, 0, 1, pix.width - 1, 0, 1, pix.height);
                    out.draw(pix, out.width - 1, 1, 0, 0, 1, pix.height);
                    // Draw opposite corners.
                    out.setRaw(0, 0, pix.getRaw(pix.width - 1, pix.height - 1));
                    out.setRaw(out.width - 1, 0, pix.getRaw(0, pix.height - 1));
                    out.setRaw(out.width - 1, out.height - 1, pix.getRaw(0, 0));
                    out.setRaw(0, out.height - 1, pix.getRaw(pix.width - 1, 0));

                    new GenRegion(reg.name, reg.file.sibling(reg.file.nameWithoutExtension() + ".floor.png"), out).save(true);
                    reg.file.delete();
                }
            }
        }));
    }
}
