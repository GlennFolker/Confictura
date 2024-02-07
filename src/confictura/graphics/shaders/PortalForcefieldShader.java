package confictura.graphics.shaders;

import arc.graphics.g3d.*;
import arc.math.geom.*;
import arc.util.*;
import confictura.content.*;
import confictura.world.planets.*;
import mindustry.graphics.g3d.*;

import static arc.Core.*;
import static confictura.graphics.CShaders.*;

/**
 * Specialized atmosphere shader to render {@link CPlanets#portal}'s artificial gravity forcefield.
 * @author GlennFolker
 */
public class PortalForcefieldShader extends HighpShader{
    private static final Mat3D mat = new Mat3D();
    private static final Vec3 axis = new Vec3(Vec3.Y).crs(PlanetGrid.create(3).tiles[0].v);

    public Camera3D camera;
    public PortalPlanet planet;

    public PortalForcefieldShader(){
        super(file("portal-forcefield.vert"), file("portal-forcefield.frag"));
    }

    @Override
    public void apply(){
        setUniformMatrix4("u_proj", camera.combined.val);
        setUniformMatrix4("u_trans", planet.getTransform(mat).rotate(axis, Vec3.Y.angle(axis)).val);
        setUniformf("u_radius", planet.forcefieldRadius);

        setUniformf("u_camPos", camera.position);
        setUniformf("u_relCamPos", Tmp.v31.set(camera.position).sub(planet.position));
        setUniformf("u_center", planet.position);
        setUniformf("u_light", Tmp.v31.set(planet.position).sub(planet.solarSystem.position).nor());

        setUniformf("u_time", Time.globalTime / 60f);
        setUniformf("u_baseColor", planet.atmosphereColor);

        planet.depthBuffer.getTexture().bind(0);
        setUniformi("u_topology", 0);
        setUniformf("u_viewport", graphics.getWidth(), graphics.getHeight());
    }
}
