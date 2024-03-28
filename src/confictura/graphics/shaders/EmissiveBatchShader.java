package confictura.graphics.shaders;

import arc.graphics.gl.*;
import arc.math.geom.*;
import arc.util.*;
import confictura.world.celestial.*;
import mindustry.type.*;

import static confictura.graphics.CShaders.*;
import static confictura.util.MathUtils.*;

public class EmissiveBatchShader extends Shader{
    private static final Mat3D mat = new Mat3D();

    public Emissive planet;

    public EmissiveBatchShader(){
        super(file("emissive-batch.vert"), file("emissive-batch.frag"));
    }

    @Override
    public void apply(){
        if(!(planet instanceof Planet p)) throw new IllegalArgumentException("'" + planet + "' isn't an instance of Planet.");

        setUniformMatrix4("u_trans", p.getTransform(mat).val);
        setUniformMatrix("u_normal", copyMatrix(mat, Tmp.m1).inv().transpose());
        setUniformf("u_light", p.solarSystem.position);
        setUniformf("u_ambientColor", p.solarSystem.lightColor.r, p.solarSystem.lightColor.g, p.solarSystem.lightColor.b);
        planet.getEmissive().bind(0);
    }
}
