package confictura.graphics.shaders;

import arc.graphics.g3d.*;
import confictura.graphics.gl.*;

import static confictura.graphics.CShaders.*;

/**
 * Specialized mesh shader to capture fragment depths.
 * @author GlFolker
 */
public class DepthShader extends Gl30Shader{
    public Camera3D camera;

    public DepthShader(){
        super(file("depth.vert"), file("depth.frag"));
    }

    @Override
    public void apply(){
        setUniformf("u_camPos", camera.position);
        setUniformf("u_camRange", camera.near, camera.far - camera.near);
    }
}
