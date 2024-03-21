package confictura.world.blocks.environment;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import confictura.graphics.g3d.*;
import confictura.world.blocks.*;
import mindustry.world.*;

import static arc.Core.*;
import static confictura.ConficturaMod.*;

public class ModelProp extends Block implements DelegateMapColor{
    public Block parent;
    public Color color;
    public Prov<Mesh>[] meshes;

    @SafeVarargs
    public ModelProp(String name, Block parent, Color color, Prov<Mesh>... meshes){
        super(name);
        this.parent = parent;
        this.color = color;
        this.meshes = meshes;
        destructible = breakable = false;
    }

    @Override
    public Block substitute(){
        return parent;
    }

    @Override
    public void drawBase(Tile tile){
        parent.drawBase(tile);

        float z = Draw.z();
        Draw.z(ModelPropDrawer.accumLayer);

        var reg = atlas.find("square-shadow");
        float size = Math.max(reg.width, reg.height) * 1.75f * reg.scl();

        Draw.color(0f, 0f, 0f, 0.65f);
        Draw.rect(reg, tile.worldx(), tile.worldy(), size * Draw.xscl, size * Draw.yscl);
        Draw.reset();

        Draw.draw(Draw.z(), () -> modelPropDrawer.draw(
            meshes[Mathf.randomSeed(tile.pos(), 0, meshes.length - 1)].get(), tile.worldx(), tile.worldy(),
            Mathf.randomSeed(tile.pos() + 1, 0, 4) * 90f, color
        ));
        Draw.z(z);
    }
}
