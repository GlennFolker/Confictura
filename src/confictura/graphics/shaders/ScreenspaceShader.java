package confictura.graphics.shaders;

import arc.math.geom.*;
import confictura.graphics.gl.*;

import static confictura.graphics.CShaders.*;

public class ScreenspaceShader extends Gl30Shader{
    public final Vec2 center = new Vec2(), scale = new Vec2();

    public ScreenspaceShader(){
        super(file("screenspace.vert"), file("screenspace.frag"));
    }

    public ScreenspaceShader trns(float centerX, float centerY, float scaleX, float scaleY){
        center.set(centerX, centerY);
        scale.set(scaleX, scaleY);
        return this;
    }

    @Override
    public void apply(){
        setUniformf("u_offset", center.x, center.y, scale.x, scale.y);
    }
}
