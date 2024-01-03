package confictura.content;

import arc.graphics.*;
import arc.math.*;
import arc.util.noise.*;
import confictura.*;
import confictura.graphics.g3d.CMeshBuilder.*;
import confictura.world.planets.*;
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
            atmosphereColor.set(0.3f, 0.6f, 1.0f, 1f);
            atmosphereOutlineColor.set(0.2f, 0.45f, 1f, 1f);

            camRadius = 0.35f;
            minZoom = 0f;

            islands = new Island[]{
                new Island(0.4f, island(0, 0.35f, -0.55f, 0.45f, monolithMid)){{
                    offset.set(0f, -0.2f, 0f);
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
            hex.high = -Interp.pow3In.apply(dst) * 0.067f + Simplex.noise2d(seeded + 1, 3f, 0.5f, 4f, x + 53.58f, y + 97.93f) * (0.4f + Interp.pow2In.apply(dst) * 0.6f) * 0.096f;

            hex.lowColor.set(Pal.darkerGray)
                .lerp(Pal.stoneGray, Interp.pow4In.apply(Simplex.noise3d(seeded + 2, 3f, 0.3f, 5f, x + 31.41f, y + 59.26f, hex.low) / 2f + 0.5f))
                .lerp(stroke, Interp.pow3In.apply(Ridged.noise3d(seeded + 3, x, y, hex.low, 1, 8f) / 2f + 0.5f));
            hex.highColor.set(Pal.darkerGray)
                .lerp(Pal.stoneGray, Interp.pow4In.apply(Simplex.noise3d(seeded + 2, 3f, 0.3f, 5f, x + 31.41f, y + 59.26f, hex.high) / 2f + 0.5f))
                .lerp(stroke, Interp.pow3In.apply(Ridged.noise3d(seeded + 3, x, y, hex.high, 1, 8f) / 2f + 0.5f));
        };
    }
}
