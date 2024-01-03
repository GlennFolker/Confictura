package confictura.graphics.shaders;

import arc.graphics.g3d.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import arc.util.*;
import confictura.content.*;
import confictura.world.planets.*;
import mindustry.graphics.g3d.*;

import static confictura.assets.CShaders.*;

/**
 * Specialized atmosphere shader to render {@link CPlanets#portal}'s artificial gravity forcefield.
 * @author GlennFolker
 */
public class PortalForcefieldShader extends Shader{
    private static final Mat3D mat = new Mat3D();
    private static final Vec3 axis = new Vec3(Vec3.Y).crs(PlanetGrid.create(3).tiles[0].v);

    public Camera3D cam;
    public PortalPlanet planet;

    public PortalForcefieldShader(){
        super(file("portal-forcefield.vert"), file("portal-forcefield.frag"));
    }

    @Override
    public void apply(){
        setUniformMatrix4("u_projection", cam.combined.val);
        setUniformMatrix4("u_model", planet.getTransform(mat).rotate(axis, Vec3.Y.angle(axis)).val);
        setUniformf("u_radius", planet.forcefieldRadius);

        setUniformf("u_camPos", cam.position);
        setUniformf("u_relCamPos", Tmp.v31.set(cam.position).sub(planet.position));
        setUniformf("u_center", planet.position);
        setUniformf("u_light", Tmp.v31.set(planet.position).sub(planet.solarSystem.position).nor());

        setUniformf("u_time", Time.globalTime / 60f);
        setUniformf("u_baseColor", planet.atmosphereColor);
    }
}
