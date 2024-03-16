package confictura.graphics.shaders;

import arc.graphics.gl.*;
import arc.math.geom.*;
import arc.util.*;
import confictura.world.planets.*;

import static confictura.graphics.CShaders.*;
import static confictura.util.MathUtils.*;

public class PortalBatchShader extends Shader{
    private static final Mat3D mat = new Mat3D();

    public PortalPlanet planet;

    public PortalBatchShader(){
        super(file("portal-batch.vert"), file("portal-batch.frag"));
    }

    @Override
    public void apply(){
        setUniformMatrix4("u_trans", planet.getTransform(mat).val);
        setUniformMatrix("u_normal", copyMatrix(mat, Tmp.m1).inv().transpose());
        setUniformf("u_light", planet.solarSystem.position);
        setUniformf("u_ambientColor", planet.solarSystem.lightColor.r, planet.solarSystem.lightColor.g, planet.solarSystem.lightColor.b);
        planet.emissiveTexture.bind(0);
    }
}
