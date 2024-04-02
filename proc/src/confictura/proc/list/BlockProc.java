package confictura.proc.list;

import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.util.*;
import arc.util.serialization.*;
import arc.util.serialization.Jval.*;
import confictura.proc.*;
import confictura.proc.GenAtlas.*;
import confictura.world.blocks.*;
import confictura.world.blocks.environment.*;
import mindustry.world.blocks.environment.*;

import java.io.*;
import java.nio.charset.*;

import static confictura.ConficturaMod.*;
import static confictura.proc.ConficturaProc.*;
import static confictura.util.StructUtils.*;
import static mindustry.Vars.*;

public class BlockProc implements Proc{
    private Jval blockColors;

    @Override
    public void init(Cons<Runnable> async){
        blockColors = Jval.newObject();

        var packer = new GenPacker();
        content.blocks().each(b -> isConfictura(b) && b.generateIcons, block -> async.get(() -> {
            try{
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

                if(block.customShadow && (block.variants == 0 ? !block.customShadowRegion.found() : any(block.variantRegions, t -> !t.found()))){
                    // Gaussian blur kernel half-width and -height.
                    int blurWidth = 64, blurHeight = 64;
                    // Gaussian filter standard deviation.
                    float deviation = 16f;

                    var blur = new float[(2 * blurWidth + 1) * (2 * blurHeight + 1)];
                    float s = 2f * deviation * deviation, nor = 0f;

                    for(int tx = -blurWidth; tx <= blurWidth; tx++){
                        for(int ty = -blurHeight; ty <= blurHeight; ty++){
                            float r = Mathf.sqrt(tx * tx + ty * ty);
                            float res = Mathf.pow(Mathf.E, -(r * r) / s) / (Mathf.pi * s);

                            blur[tx + blurWidth + (ty + blurHeight) * (2 * blurWidth + 1)] = res;
                            nor += res;
                        }
                    }

                    for(int i = 0; i < blur.length; i++) blur[i] /= nor;

                    int index = 0;
                    for(var variant : block.variants == 0 ? iter(block.fullIcon) : iter(block.variantRegions)){
                        var reg = atlas.conv(variant);
                        var pix = reg.pixmap();

                        var ref = new Pixmap(pix.width + blurWidth * 2, pix.height + blurHeight * 2);
                        ref.draw(pix, blurWidth, blurHeight);
                        Pixmaps.bleed(ref, Integer.MAX_VALUE);

                        var shadow = new Pixmap(ref.width, ref.height);
                        ref.each((x, y) -> {
                            float a = 0f;
                            for(int tx = -blurWidth; tx <= blurWidth; tx++){
                                for(int ty = -blurHeight; ty <= blurHeight; ty++){
                                    float factor = blur[tx + blurWidth + (ty + blurHeight) * (2 * blurWidth + 1)];
                                    a += ((ref.get(x + tx, y + ty) & 0xff) / 255f) * factor;
                                }
                            }

                            shadow.setRaw(x, y, 0xffffff00 | (int)(a * 255f));
                        });

                        var name = block.name.substring("confictura-".length()) + "-shadow";
                        if(block.variants != 0) name += ++index;

                        new GenRegion(name, reg.file.sibling(name + ".png"), shadow).save(true);
                        ref.dispose();
                    }
                }

                block.load();
                if(block instanceof DelegateMapColor map){
                    synchronized(BlockProc.class){
                        var sub = map.substitute();
                        if(isConfictura(sub)){
                            blockColors.put(block.name.substring("confictura-".length()), sub.name.substring("confictura-".length()));
                        }else{
                            throw new IllegalArgumentException(Strings.format("Block '@' has non-Confictura map color substitution '@'.", block.name, sub.name));
                        }
                    }
                }else{
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
                }
            }catch(Throwable t){
                Log.warn("Skipping '@': @", block.name.substring("confictura-".length()), Strings.getStackTrace(Strings.getFinalCause(t)));
            }
        }));
    }

    @Override
    public void finish(){
        synchronized(BlockProc.class){
            var map = blockColors.asObject();
            for(var e : map){
                if(e.value.isString()) map.put(e.key, map.get(e.value.asString()));
            }

            var out = assetsDir.child("meta").child("confictura").child("block-colors.json");
            try(var writer = new OutputStreamWriter(out.write(false, 4096), StandardCharsets.UTF_8)){
                blockColors.writeTo(writer, Jformat.formatted);
            }catch(IOException e){
                throw new RuntimeException(e);
            }

            blockColors = null;
        }
    }
}
