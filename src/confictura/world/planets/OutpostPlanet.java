package confictura.world.planets;

import arc.graphics.*;
import arc.math.geom.*;
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
    public Island[] islands = {};

    public OutpostPlanet(String name, Planet parent, float radius){
        super(name, parent, radius, 0);

        meshLoader = OutpostMesh::new;
    }

    public static class Island{
        public float radius;
        public float resolution;
        public IslandShaper shaper;

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

            for(var mesh : islandMeshes){
                shader.setUniformMatrix4("u_trans", transform.val);
                mesh.render(shader, Gl.triangles);
            }
        }

        /*
        @Override
        public void render(PlanetParams params, Mat3D projection, Mat3D transform){
            preRender(params);

            Shaders.planet.bind();
            Shaders.planet.setUniformMatrix4("u_proj", projection.val);
            Shaders.planet.apply();

            for(var island : islands){
                mat3.set(island.offset, quat.setFromAxis(Vec3.Y, island.rotation));
                mat2.set(transform).mul(mat3);

                if(island.hoverMag > 0.0001f){
                    mat3.set(Tmp.v31.set(0f, -Mathf.absin(Time.globalTime, island.hoverScl, island.hoverMag), 0f), quat.idt());
                    mat2.mul(mat3);
                }

                Shaders.planet.setUniformMatrix4("u_trans", mat2.val);
                island.mesh.render(Shaders.planet, Gl.triangles);
            }
        }
         */
    }

    /*
    {
        islands = new Island[]{
            new Island(){{
                mesh = CMeshBuilder.merge(
                    CMeshBuilder.floatingIsland(
                        Tmp.c1.set(Pal.stoneGray).a(0.4f), radius + 0.1f, radius * 4.2f,
                        1f, 4, 0.5, 1,
                        0
                    ),
                    CMeshBuilder.build(builder -> {
                        //float scl = 0.05f;
                    }),
                    true, true
                );
                offset = new Vec3(0f, -0.02f, 0f);
                rotation = hoverScl = hoverMag = 0f;
            }},
            new Island(){{
                mesh = CMeshBuilder.floatingIsland(
                    Tmp.c1.set(Pal.stoneGray).lerp(Pal.lancerLaser, 0.3f).a(0.6f), radius / 2f, radius * 0.8f,
                    1f, 4, 0.5, 1.2,
                    1
                );
                offset = new Vec3(0.2f, -0.15f, 0.15f);
                rotation = 60f;
                hoverScl = 100f;
                hoverMag = 0.03f;
            }},
            new Island(){{
                mesh = CMeshBuilder.floatingIsland(
                    Tmp.c1.set(Pal.stoneGray).lerp(Pal.lancerLaser, 0.12f).a(0.5f), radius / 3f, radius * 0.8f,
                    1f, 4, 0.5, 1.2,
                    2
                );
                offset = new Vec3(0.2f, -0.1f, -0.13f);
                rotation = 45f;
                hoverScl = 80f;
                hoverMag = 0.025f;
            }},
            new Island(){{
                mesh = CMeshBuilder.floatingIsland(
                    Tmp.c1.set(Pal.stoneGray).lerp(Pal.place, 0.1f).a(0.4f), radius / 3.75f, radius * 0.8f,
                    1f, 4, 0.5, 1.2,
                    3
                );
                offset = new Vec3(-0.05f, -0.22f, 0.18f);
                rotation = 30f;
                hoverScl = 70f;
                hoverMag = 0.02f;
            }},
            new Island(){{
                mesh = CMeshBuilder.floatingIsland(
                    Tmp.c1.set(Pal.stoneGray).lerp(Pal.accent, 0.12f).a(0.5f), radius / 4f, radius * 0.5f,
                    1f, 4, 0.5, 1.2,
                    4
                );
                offset = new Vec3(-0.16f, -0.18f, 0.14f);
                rotation = 75f;
                hoverScl = 60f;
                hoverMag = 0.017f;
            }},
            new Island(){{
                mesh = CMeshBuilder.floatingIsland(
                    Tmp.c1.set(Pal.stoneGray).lerp(Pal.accent, 0.3f).a(0.6f), radius / 3.2f, radius * 0.7f,
                    1f, 4, 0.5, 1.2,
                    4
                );
                offset = new Vec3(0.03f, -0.2f, -0.15f);
                rotation = 75f;
                hoverScl = 60f;
                hoverMag = 0.02f;
            }},
        };
    }
     */
}
