package confictura.graphics.shaders;

import arc.math.geom.*;
import arc.util.*;
import confictura.graphics.gl.*;
import confictura.world.celestial.*;

import static confictura.graphics.CShaders.*;
import static confictura.util.MathUtils.*;

public class EmissiveBatchShader extends Gl30Shader{
    private static final Mat3D mat = new Mat3D();

    public EmissiveObject planet;

    public EmissiveBatchShader(){
        super(file("emissive-batch.vert"), file("emissive-batch.frag"));
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
