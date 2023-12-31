package confictura.world.planets;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import confictura.content.*;
import confictura.graphics.g3d.*;
import confictura.graphics.g3d.CMeshBuilder.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;

/**
 * The {@link CPlanets#outpost outpost} celestial object. Composed of floating islands, 9 sectors arranged on the
 * surface, and an artificial gravity force field.
 * @author GlennFolker
 */
public class OutpostPlanet extends Planet{
    private static final Mat3D mat1 = new Mat3D(), mat2 = new Mat3D();
    private static final Quat quat = new Quat();
    private static final Vec3 v1 = new Vec3();

    public Island[] islands = {};

    public OutpostPlanet(String name, Planet parent, float radius){
        super(name, parent, radius, 0);

        meshLoader = OutpostMesh::new;
    }

    public static class Island{
        public float radius;
        public float resolution;
        public IslandShaper shaper;

        public final Vec3 offset = new Vec3();
        public float rotation;
        public float hoverMag, hoverScale;

        public Island(float radius, IslandShaper shaper){
            this(radius, 50f, shaper);
        }

        public Island(float radius, float resolution, IslandShaper shaper){
            this.radius = radius;
            this.resolution = resolution;
            this.shaper = shaper;
        }
    }

    public class OutpostMesh implements GenericMesh{
        public Mesh[] islandMeshes;

        public OutpostMesh(){
            islandMeshes = new Mesh[islands.length];
            for(int i = 0; i < islands.length; i++){
                var island = islands[i];
                islandMeshes[i] = CMeshBuilder.island(island.radius, island.resolution, island.shaper);
            }
        }

        @Override
        public void render(PlanetParams params, Mat3D projection, Mat3D transform){
            var shader = Shaders.planet;
            shader.planet = OutpostPlanet.this;
            shader.lightDir.set(solarSystem.position).sub(position).rotate(Vec3.Y, getRotation()).nor();
            shader.ambientColor.set(solarSystem.lightColor);

            shader.bind();
            shader.setUniformMatrix4("u_proj", projection.val);
            shader.apply();

            for(int i = 0, len = islands.length; i < len; i++){
                var island = islands[i];
                var mesh = islandMeshes[i];

                mat1.set(island.offset, quat.setFromAxis(Vec3.Y, island.rotation));
                mat2.set(transform).mul(mat1);

                mat1.set(v1.set(0f, -Mathf.absin(Time.globalTime, island.hoverScale, island.hoverMag), 0f), quat.idt());
                mat2.mul(mat1);

                shader.setUniformMatrix4("u_trans", mat2.val);
                mesh.render(shader, Gl.triangles);
            }
        }
    }
}
