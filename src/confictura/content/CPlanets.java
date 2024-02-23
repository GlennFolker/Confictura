package confictura.content;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.noise.*;
import confictura.*;
import confictura.graphics.*;
import confictura.graphics.g3d.CMeshBuilder.*;
import confictura.util.*;
import confictura.world.planets.*;
import gltfrenzy.model.*;
import mindustry.content.*;
import mindustry.graphics.*;
import mindustry.type.*;

import static confictura.graphics.CPal.*;

/**
 * Defines the {@link Planet planets} and other celestial objects this mod offers.
 * @author GlennFolker
 */
public final class CPlanets{
    public static Planet portal;

    private CPlanets(){
        throw new AssertionError();
    }

    /** Instantiates all contents. Called in the main thread in {@link ConficturaMod#loadContent()}. */
    public static void load(){
        portal = new PortalPlanet("portal", Planets.sun, 0.6f){{
            var mat = new Mat3D();
            var nor = new Vec3();

            atmosphereColor.set(0x3366e5ff);
            atmosphereOutlineColor.set(0x1966ffff);
            icon = "host";
            iconColor = monolithLighter;

            camRadius = -0.067f;
            minZoom = 0.75f;

            sectorColor = monolithLighter;
            sectorOffset = -0.15f;
            sectorRadius = 0.08f;
            sectorInnerRadius = 0.15f;
            sectorDistance = 0.275f;

            float structureOffset = -0.2125f, structureScale = 0.05f;

            period = 240f;
            drawStructure = (shader, transform) -> {
                Node base = CModels.portalBase, cage = CModels.portalCage;
                base.localTrns.translation.set(0f, structureOffset, 0f);
                base.localTrns.rotation.idt();
                base.localTrns.scale.set(structureScale, structureScale, structureScale);

                cage.localTrns.translation.set(0f, 5f, 0f);
                cage.localTrns.rotation.set(Vec3.Y, period(Interp.pow5) * 90f);
                cage.localTrns.scale.set(1f, 1f, 1f);
                base.update();

                for(var mesh : base.mesh.containers){
                    shader.setUniformMatrix4("u_trans", mat.set(transform).mul(base.globalTrns).val);
                    shader.setUniformMatrix("u_normal", MathUtils.copyMatrix(mat, Tmp.m1).inv().transpose());
                    mesh.render(shader);
                }

                for(var mesh : cage.mesh.containers){
                    shader.setUniformMatrix4("u_trans", mat.set(transform).mul(cage.globalTrns).val);
                    shader.setUniformMatrix("u_normal", MathUtils.copyMatrix(mat, Tmp.m1).inv().transpose());
                    mesh.render(shader);
                }
            };

            emissions = new Color[]{monolithDark};
            drawEmissive = batch -> {
                var light = emissiveRegions[0];

                int seg = 12;
                for(int i = 0; i < seg; i++){
                    float stroke = (1f - period(0.5f, 0f, 0.5f, Interp.pow5In)) * 0.0125f;
                    Tmp.v1.trns(i * 360f / seg, stroke);
                    Tmp.v2.trns((i + 1) * 360f / seg, stroke);

                    Tmp.v31.set(Tmp.v1.x, structureOffset, Tmp.v1.y);
                    Tmp.v32.set(Tmp.v2.x, structureOffset, Tmp.v2.y);
                    Tmp.v33.set(Tmp.v32.x, period(0.5f, 0f, 0.36f, Interp.pow3Out) * 14.25f * structureScale + structureOffset, Tmp.v32.z);
                    Tmp.v34.set(Tmp.v31.x, Tmp.v33.y, Tmp.v31.z);

                    MathUtils.normal(nor, Tmp.v33, Tmp.v32, Tmp.v31);
                    Tmp.c1.set(monolithDarker).a(1f - period(0.5f, 0f, 0.5f, Interp.pow5In));
                    Tmp.c2.set(Tmp.c1).a(0f);

                    batch.color(Tmp.c2);
                    batch.normal(nor);
                    batch.texCoord(light.u, light.v);
                    batch.vertex(Tmp.v34);

                    batch.color(Tmp.c2);
                    batch.normal(nor);
                    batch.texCoord(light.u2, light.v);
                    batch.vertex(Tmp.v33);

                    batch.color(Tmp.c1);
                    batch.normal(nor);
                    batch.texCoord(light.u2, light.v2);
                    batch.vertex(Tmp.v32);

                    batch.color(Tmp.c1);
                    batch.normal(nor);
                    batch.texCoord(light.u2, light.v2);
                    batch.vertex(Tmp.v32);

                    batch.color(Tmp.c1);
                    batch.normal(nor);
                    batch.texCoord(light.u, light.v2);
                    batch.vertex(Tmp.v31);

                    batch.color(Tmp.c2);
                    batch.normal(nor);
                    batch.texCoord(light.u, light.v);
                    batch.vertex(Tmp.v34);
                }
            };

            islands = new Island[]{
                new Island(0.4f, island(0, 0.35f, -0.55f, 0.45f, monolithMid)){{
                    offset.set(0f, -0.23f, 0f);
                }},
                new Island(0.4f, island(1, 0.18f, -0.2f, 0.56f, monolithDarker)){{
                    offset.set(0.32f, -0.35f, 0.2f);
                    rotation = 60f;
                    hoverScale = 100f;
                    hoverMag = 0.06f;
                }},
                new Island(0.4f, island(2, 0.2f, -0.27f, 0.5f, monolithDark)){{
                    offset.set(0.3f, -0.47f, -0.18f);
                    rotation = 45f;
                    hoverScale = 80f;
                    hoverMag = 0.05f;
                }},
                new Island(0.4f, island(3, 0.16f, -0.22f, 0.47f, monolithMid)){{
                    offset.set(-0.1f, -0.44f, 0.36f);
                    rotation = 30f;
                    hoverScale = 70f;
                    hoverMag = 0.04f;
                }},
                new Island(0.4f, island(4, 0.17f, -0.2f, 0.35f, monolithLight)){{
                    offset.set(-0.32f, -0.5f, 0f);
                    rotation = 75f;
                    hoverScale = 60f;
                    hoverMag = 0.034f;
                }},
                new Island(0.4f, island(5, 0.16f, -0.24f, 0.4f, monolithLighter)){{
                    offset.set(-0.23f, -0.4f, -0.32f);
                    rotation = 75f;
                    hoverScale = 60f;
                    hoverMag = 0.04f;
                }}
            };
        }};
    }

    private static IslandShaper island(int seed, float dstScale, float depth, float depthScale, Color stroke){
        int seeded = seed * 4;
        return (x, y, hex) -> {
            float dst = Mathf.len(x, y) / dstScale;
            hex.low = depth + dst * depthScale + Simplex.noise2d(seeded, 3f, 0.5f, 4.5f, x + 31.41f, y + 59.26f) * (0.3f + Interp.pow2In.apply(dst) * 0.7f) * 0.1f;
            hex.high = -Interp.pow3In.apply(dst) * 0.067f + Simplex.noise2d(seeded + 1, 3f, 0.5f, 4f, x + 53.58f, y + 97.93f) * (0.4f + Interp.pow2In.apply(dst) * 0.6f) * 0.08f;

            hex.lowColor.set(Pal.darkerGray)
                .lerp(Pal.stoneGray, Interp.pow4In.apply(Simplex.noise3d(seeded + 2, 3f, 0.3f, 5f, x + 31.41f, y + 59.26f, hex.low) / 2f + 0.5f))
                .lerp(stroke.r, stroke.g, stroke.b, 0f, Interp.pow3In.apply(Ridged.noise3d(seeded + 3, x, y, hex.low, 1, 8f) / 2f + 0.5f));
            hex.highColor.set(Pal.darkerGray)
                .lerp(Pal.stoneGray, Interp.pow4In.apply(Simplex.noise3d(seeded + 2, 3f, 0.3f, 5f, x + 31.41f, y + 59.26f, hex.high) / 2f + 0.5f))
                .lerp(stroke.r, stroke.g, stroke.b, 0f, Interp.pow3In.apply(Ridged.noise3d(seeded + 3, x, y, hex.high, 1, 8f) / 2f + 0.5f));
        };
    }
}
