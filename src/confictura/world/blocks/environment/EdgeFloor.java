package confictura.world.blocks.environment;

import arc.graphics.*;
import arc.graphics.g2d.*;
import confictura.graphics.g2d.*;
import mindustry.content.*;
import mindustry.graphics.*;
import mindustry.graphics.MultiPacker.*;
import mindustry.world.blocks.environment.*;

import static arc.Core.*;
import static mindustry.Vars.*;

/**
 * A custom {@linkplain Floor floor} with per-variant edges.
 * @author GlennFolker
 */
public class EdgeFloor extends Floor{
    public TextureRegion[][][] edges;
    /** If {@code true}, edges will use regions for the originating tile instead of the target tile. */
    public boolean absolute = false;

    public EdgeFloor(String name){
        super(name);
    }

    @Override
    public void load(){
        super.load();

        int size = (int)(tilesize / Draw.scl);
        edges = new TextureRegion[variants][][];
        for(int i = 0; i < variants; i++){
            var edge = atlas.find(name + "-edge" + (i + 1));
            if(!edge.found()) continue;

            edges[i] = edge.split(size, size);
        }
    }

    @Override
    public void createIcons(MultiPacker packer){
        if(blendGroup != this){
            super.createIcons(packer);
        }else{
            // Make the super implementation not generate edges.
            var blend = blendGroup;
            blendGroup = Blocks.air;

            super.createIcons(packer);
            blendGroup = blend;

            // Instead, create individual edges for each variant.
            for(int i = 0; i < variantRegions.length; i++){
                if(packer.has(PageType.environment, name + "-edge" + (i + 1))) continue;

                // These two are to be excluded from the final texture atlas.
                var stencil = atlas.find(name + "-edge-stencil" + (i + 1), "edge-stencil");
                var template = atlas.find(name + "-edge-template" + (i + 1), variantRegions[i]);

                var edge = atlas.getPixmap(stencil);
                var result = new Pixmap(edge.width, edge.height);
                var image = atlas.getPixmap(template);
                result.each((x, y) -> result.setRaw(x, y, Color.muli(edge.getRaw(x, y), image.getRaw(x % image.width, y % image.height))));

                packer.add(PageType.environment, name + "-edge" + (i + 1), result);
                result.dispose();

                if(atlas instanceof FreeableAtlas free){
                    free.delete(stencil);
                    if(template != variantRegions[i]) free.delete(template);
                }
            }
        }
    }

    @Override
    public TextureRegion[][] edges(int x, int y){
        return blendGroup != this ? super.edges(x, y) : edges[variant(x, y)];
    }

    @Override
    protected TextureRegion edge(int x, int y, int rx, int ry){
        return (absolute ? edges(x - rx, y - ry) : edges(x, y))[rx][2 - ry];
    }
}
