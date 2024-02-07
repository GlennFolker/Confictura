package confictura.graphics.shaders;

import arc.graphics.g3d.*;
import arc.graphics.gl.*;

import static confictura.graphics.CShaders.*;

/**
 * Specialized mesh shader to capture fragment depths.
 * @author GlennFolker
 */
public class DepthShader extends Shader{
    public Camera3D camera;

    public DepthShader(){
        super(file("depth.vert"), file("depth.frag"));
    }

    @Override
    public void apply(){
        setUniformf("u_camPos", camera.position);
    }
}
