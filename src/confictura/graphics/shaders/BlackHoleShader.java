package confictura.graphics.shaders;

import arc.graphics.g3d.*;
import arc.math.geom.*;
import arc.util.*;
import confictura.graphics.gl.*;
import confictura.world.celestial.*;

import static confictura.graphics.CShaders.*;

public class BlackHoleShader extends Gl30Shader{
    private static final Mat3D mat = new Mat3D();

    public Camera3D camera;
    public BlackHole planet;

    public BlackHoleShader(){
        super(file("black-hole.vert"), file("black-hole.frag"));
    }

    @Override
    public void apply(){
        setUniformMatrix4("u_proj", camera.combined.val);
        setUniformMatrix4("u_invProj", camera.invProjectionView.val);
        setUniformf("u_far", camera.far);
        setUniformMatrix4("u_trans", planet.getTransform(mat).val);

        setUniformf("u_camPos", camera.position);
        setUniformf("u_relCamPos", Tmp.v31.set(camera.position).sub(planet.position));
        setUniformf("u_center", planet.position);

        setUniformf("u_radius", planet.radius);
        setUniformf("u_horizon", planet.horizon);

        planet.ref.getTexture().bind(0);
        setUniformi("u_ref", 0);
    }
}
