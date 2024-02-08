package confictura.graphics.shaders;

import arc.graphics.g3d.*;
import arc.graphics.gl.*;

import static confictura.graphics.CShaders.*;

/**
 * Draws an infinite plane grid on the planet dialog.
 * @author GlennFolker
 */
public class PlanetDebugShader extends Shader{
    public Camera3D camera;

    public PlanetDebugShader(){
        super(file("planet-debug.vert"), file("planet-debug.frag"));
    }

    @Override
    public void apply(){
        setUniformMatrix4("u_invProj", camera.invProjectionView.val);
    }
}
