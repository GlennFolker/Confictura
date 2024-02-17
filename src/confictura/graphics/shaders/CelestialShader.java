package confictura.graphics.shaders;

import arc.graphics.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import mindustry.graphics.Shaders.*;

import static confictura.graphics.CShaders.*;

/**
 * Similar to {@link PlanetShader}, but properly calculates the light normals.
 * @author GlennFolker
 */
public class CelestialShader extends Shader{
    public Vec3 light = new Vec3();
    public Color ambientColor = new Color();
    public Vec3 camPos = new Vec3();

    public CelestialShader(){
        super(file("celestial.vert"), file("celestial.frag"));
    }

    @Override
    public void apply(){
        setUniformf("u_light", light);
        setUniformf("u_ambientColor", ambientColor.r, ambientColor.g, ambientColor.b);
        setUniformf("u_camPos", camPos);
    }
}
