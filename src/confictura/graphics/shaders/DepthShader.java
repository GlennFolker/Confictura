package confictura.graphics.shaders;

import arc.graphics.gl.*;
import arc.math.geom.*;

import static confictura.graphics.CShaders.*;

/**
 * Specialized mesh shader to capture fragment depths.
 * @author GlennFolker
 */
public class DepthShader extends Shader{
    public Vec3 camPos = new Vec3();

    public DepthShader(){
        super(file("depth.vert"), file("depth.frag"));
    }

    @Override
    public void apply(){
        setUniformf("u_camPos", camPos);
    }
}
