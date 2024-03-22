package confictura.world.blocks.environment;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import confictura.graphics.g3d.*;
import confictura.world.blocks.*;
import mindustry.world.*;

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
        alwaysReplace = breakable = false;
        solid = instantBuild = allowRectanglePlacement = true;
    }

    @Override
    public boolean isStatic(){
        return true;
    }

    @Override
    public Block substitute(){
        return parent;
    }

    @Override
    public void drawBase(Tile tile){
        parent.drawBase(tile);
        float z = Draw.z();

        Draw.draw(ModelPropDrawer.accumLayer, () -> modelPropDrawer.draw(
            meshes[Mathf.randomSeed(tile.pos(), 0, meshes.length - 1)].get(), tile.worldx(), tile.worldy(),
            Mathf.randomSeed(tile.pos() + 1, 0, 4) * 90f, color
        ));
        Draw.z(z);
    }
}
