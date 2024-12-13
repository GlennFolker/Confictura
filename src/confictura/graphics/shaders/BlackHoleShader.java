package confictura.graphics.shaders;

import arc.graphics.Cubemap.*;
import arc.graphics.g3d.*;
import arc.math.geom.*;
import arc.util.*;
import confictura.graphics.gl.*;
import confictura.world.celestial.*;

import static arc.Core.*;
import static confictura.graphics.CShaders.*;

public class BlackHoleShader extends Gl30Shader{
    private static final Mat3D mat = new Mat3D();

    public Camera3D camera;
    public Mat3D[] cubemapView = new Mat3D[CubemapSide.values().length];
    public BlackHole planet;

    public BlackHoleShader(){
        super(file("black-hole.vert"), file("black-hole.frag"));
        for(int i = 0; i < cubemapView.length; i++){
            cubemapView[i] = new Mat3D();
        }
    }

    @Override
    public void apply(){
        setUniformf("u_camPos", camera.position);
        setUniformf("u_viewport", graphics.getWidth(), graphics.getHeight());
        setUniformf("u_relCamPos", Tmp.v31.set(camera.position).sub(planet.position));
        setUniformf("u_center", planet.position);

        setUniformf("u_radius", planet.radius);
        setUniformf("u_horizon", planet.horizon);

        for(int i = 0; i < cubemapView.length; i++){
            setUniformMatrix4("u_cubeView[" + i + "]", cubemapView[i].val);
            setUniformMatrix4("u_cubeInvView[" + i + "]", mat.set(cubemapView[i]).inv().val);
        }

        setUniformMatrix4("u_proj", camera.combined.val);
        setUniformMatrix4("u_invProj", camera.invProjectionView.val);
        setUniformf("u_depthRange", camera.near, camera.far);

        planet.pov.getDepthTexture().bind(1);
        planet.pov.getTexture().bind(0);
        setUniformi("u_depth", 1);
        setUniformi("u_ref", 0);
    }
}
