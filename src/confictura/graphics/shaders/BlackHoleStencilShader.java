package confictura.graphics.shaders;

import arc.graphics.g3d.*;
import arc.math.geom.*;
import confictura.graphics.gl.*;
import confictura.world.celestial.*;

import static arc.Core.*;
import static confictura.graphics.CShaders.*;

public class BlackHoleStencilShader extends Gl30Shader{
    private static final Mat3D mat = new Mat3D();
    private static final Vec3 v1 = new Vec3();

    public Camera3D camera;
    public BlackHole planet;

    public BlackHoleStencilShader(){
        super(file("black-hole-stencil.vert"), file("black-hole-stencil.frag"));
    }

    @Override
    public void apply(){
        setUniformMatrix4("u_invProj", mat.set(camera.projection).inv().val);
        setUniformMatrix4("u_invProjView", camera.invProjectionView.val);
        setUniformf("u_camPos", camera.position);
        setUniformf("u_relCamPos", v1.set(camera.position).sub(planet.position));
        setUniformf("u_viewport", graphics.getWidth(), graphics.getHeight());

        setUniformf("u_radius", planet.radius);

        planet.orbit.getTexture().bind(1);
        planet.orbit.getDepthTexture().bind(0);

        setUniformi("u_src", 1);
        setUniformi("u_srcDepth", 0);
    }
}
