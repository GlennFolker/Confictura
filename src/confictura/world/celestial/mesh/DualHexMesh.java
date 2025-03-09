package confictura.world.celestial.mesh;

import arc.graphics.*;
import arc.math.geom.*;
import arc.util.*;
import confictura.graphics.g3d.*;
import confictura.graphics.shaders.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;

import static confictura.util.MathUtils.*;
import static mindustry.Vars.*;

public class DualHexMesh implements GenericMesh{
    public Mesh mesh;
    public Planet planet;
    public CelestialShader shader;

    public DualHexMesh(Planet planet, int divisions, CelestialShader shader, DualHexMesher mesh){
        this.planet = planet;
        this.shader = shader;
        this.mesh = CMeshBuilder.buildDualHex(mesh, divisions, planet.radius);
    }

    @Override
    public void render(PlanetParams params, Mat3D projection, Mat3D transform){
        shader.camPos.set(renderer.planets.cam.position);
        shader.light.set(planet.solarSystem.position);
        shader.ambientColor.set(planet.solarSystem.lightColor);

        shader.bind();
        shader.setUniformMatrix4("u_proj", renderer.planets.cam.combined.val);
        shader.setUniformMatrix4("u_trans", transform.val);
        shader.setUniformMatrix("u_normal", copyMatrix(transform, Tmp.m1).inv().transpose());
        shader.apply();

        mesh.render(shader, Gl.triangles);
    }

    public interface DualHexMesher{
        void topology(Vec3 position, Vec2 out);

        void color(Vec3 position, Color outHigh, Color outLow);
    }
}
