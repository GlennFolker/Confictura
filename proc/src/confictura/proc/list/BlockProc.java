package confictura.proc.list;

import arc.func.*;
import arc.graphics.*;
import arc.util.serialization.*;
import arc.util.serialization.Jval.*;
import confictura.*;
import confictura.proc.*;
import confictura.proc.GenAtlas.*;
import confictura.world.blocks.environment.*;
import mindustry.world.blocks.environment.*;

import java.io.*;
import java.nio.charset.*;

import static confictura.proc.ConficturaProc.*;
import static confictura.util.StructUtils.*;
import static mindustry.Vars.*;

public class BlockProc implements Proc{
    private Jval blockColors;

    @Override
    public void init(Cons<Runnable> async){
        blockColors = Jval.newObject();

        var packer = new GenPacker();
        content.blocks().each(ConficturaMod::isConfictura, block -> async.get(() -> {
            block.init();
            block.loadIcon();
            block.load();

            block.createIcons(packer);
            block.load();

            if(block instanceof EdgeFloor floor){
                for(var variant : chain(iter(floor.variantRegions), iter(floor.editorVariantRegions()))){
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

            block.load();
            var icon = atlas.conv(block.fullIcon).pixmap();

            boolean hollow = false;
            Color average = new Color(), col = new Color();

            for(int x = 0, width = icon.width; x < width; x++){
                for(int y = 0, height = icon.height; y < height; y++){
                    col.set(icon.getRaw(x, y));
                    average.r += col.r;
                    average.g += col.g;
                    average.b += col.b;
                    average.a += col.a;
                    if(col.a < 0.9f) hollow = true;
                }
            }

            float a = average.a;
            average.mul(1f / a);

            if(block instanceof Floor floor && !floor.wallOre){
                average.mul(0.77f);
            }else{
                average.mul(1.1f);
            }

            average.a = hollow ? 0f : 1f;
            synchronized(BlockProc.class){
                blockColors.put(block.name.substring("confictura-".length()), average.rgba());
            }
        }));
    }

    @Override
    public void finish(){
        var out = assetsDir.child("meta").child("confictura").child("block-colors.json");
        try(var writer = new OutputStreamWriter(out.write(false, 4096), StandardCharsets.UTF_8)){
            blockColors.writeTo(writer, Jformat.formatted);
        }catch(IOException e){
            throw new RuntimeException(e);
        }

        blockColors = null;
    }
}
