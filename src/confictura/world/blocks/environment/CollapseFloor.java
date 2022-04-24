package confictura.world.blocks.environment;

import arc.*;
import arc.graphics.g2d.*;
import confictura.assets.*;
import confictura.graphics.*;
import mindustry.game.*;
import mindustry.graphics.*;
import mindustry.graphics.MultiPacker.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;

import java.util.*;

import static mindustry.Vars.*;

public class CollapseFloor extends Floor{
    public final int index;

    private static int lastIndex;
    private static CollapseFloor[] all = new CollapseFloor[0];

    public CollapseFloor(String name){
        super(name);
        variants = 0;
        index = lastIndex++;

        CollapseFloor[] prev = all;
        all = new CollapseFloor[lastIndex];
        all[lastIndex - 1] = this;
        System.arraycopy(prev, 0, all, 0, prev.length);
    }

    public static int max(){
        return lastIndex;
    }

    public static CollapseFloor get(int index){
        return all[index];
    }

    @Override
    public void init(){
        super.init();
        cacheLayer = CCacheLayer.collapse;
    }

    @Override
    public void load(){
        variantRegions = new TextureRegion[1];
        teamRegion = region = variantRegions[0] = Core.atlas.find("confictura-white");
        edgeRegion = Core.atlas.find("clear");

        teamRegions = new TextureRegion[Team.all.length];
        Arrays.fill(teamRegions, teamRegion);

        int size = (int)(tilesize / Draw.scl);
        edges = Core.atlas.find("edge-stencil").split(size, size);
    }

    @Override
    public void loadIcon(){
        fullIcon = uiIcon = Core.atlas.find("confictura-white");
    }

    @Override
    public void createIcons(MultiPacker packer){
        PixmapRegion reg = Core.atlas.getPixmap("confictura-white");
        packer.add(PageType.editor, name + "-icon-editor", reg);
        packer.add(PageType.editor, "editor-" + name, reg);
    }

    @Override
    public void drawBase(Tile tile){
        Draw.color((float)index / CShaders.collapse.draws.length, 0f, 0f, 1f);
        Draw.rect(region, tile.worldx(), tile.worldy(), 8f, 8f);
        Draw.color();
    }
}
