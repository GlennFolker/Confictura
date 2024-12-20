package confictura.graphics.shaders;

import arc.graphics.g3d.*;
import arc.math.geom.*;
import arc.util.*;
import confictura.graphics.gl.*;
import confictura.world.celestial.*;

import static arc.Core.*;
import static confictura.graphics.CShaders.*;

/**
 * An atmosphere shader that incorporates the planet shape in a form of depth texture. Better quality, but at the little
 * cost of performance.
 * @author GlFolker
 */
public class DepthAtmosphereShader extends Gl30Shader{
    private static final Mat3D mat = new Mat3D();

    public Camera3D camera;
    public AtmospherePlanet planet;

    public DepthAtmosphereShader(){
        super(file("depth-atmosphere.vert"), file("depth-atmosphere.frag"));
    }

    @Override
    public void apply(){
        setUniformMatrix4("u_projView", camera.combined.val);
        setUniformMatrix4("u_invProj", mat.set(camera.projection).inv().val);
        setUniformMatrix4("u_trans", planet.getTransform(mat).val);

        setUniformf("u_camPos", camera.position);
        setUniformf("u_relCamPos", Tmp.v31.set(camera.position).sub(planet.position));
        setUniformf("u_depthRange", camera.near, camera.far);
        setUniformf("u_center", planet.position);
        setUniformf("u_light", planet.getLightNormal());
        setUniformf("u_color", planet.atmosphereColor.r, planet.atmosphereColor.g, planet.atmosphereColor.b);

        setUniformf("u_innerRadius", planet.radius + planet.atmosphereRadIn);
        setUniformf("u_outerRadius", planet.radius + planet.atmosphereRadOut);

        planet.buffer.getDepthTexture().bind(0);
        setUniformi("u_topology", 0);
        setUniformf("u_viewport", graphics.getWidth(), graphics.getHeight());
    }
}
