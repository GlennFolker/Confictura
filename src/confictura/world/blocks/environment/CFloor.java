package confictura.world.blocks.environment;

import arc.graphics.*;
import arc.graphics.g2d.*;
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
public class CFloor extends Floor{
    public TextureRegion[][][] edges;

    public CFloor(String name){
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
            var edge = atlas.getPixmap(atlas.find(name + "-edge-stencil", "edge-stencil"));
            for(int i = 0; i < variantRegions.length; i++){
                var image = atlas.getPixmap(variantRegions[i]);
                var result = new Pixmap(edge.width, edge.height);
                result.each((x, y) -> result.setRaw(x, y, Color.muli(edge.getRaw(x, y), image.getRaw(x % image.width, y % image.height))));

                packer.add(PageType.environment, name + "-edge" + (i + 1), result);
            }
        }
    }

    @Override
    protected TextureRegion[][] edges(int x, int y){
        return edges[variant(x, y)];
    }
}
