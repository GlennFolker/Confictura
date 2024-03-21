package confictura.world.blocks.environment;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import confictura.graphics.g3d.*;
import mindustry.world.*;

import static confictura.ConficturaMod.*;

public class ModelProp extends Block{
    public Prov<Mesh> mesh;

    public ModelProp(String name, Prov<Mesh> mesh){
        super(name);
        this.mesh = mesh;
        destructible = breakable = false;
    }

    @Override
    public void drawBase(Tile tile){
        Draw.draw(ModelPropDrawer.accumLayer, () -> modelPropDrawer.draw(mesh.get(), tile.drawx(), tile.drawy(), Mathf.randomSeed(tile.pos(), 0, 4) * 90f));
    }
}
