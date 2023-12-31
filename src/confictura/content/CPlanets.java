package confictura.content;

import arc.math.*;
import arc.util.noise.*;
import confictura.*;
import confictura.world.planets.*;
import mindustry.content.*;
import mindustry.graphics.*;
import mindustry.type.*;

/**
 * Defines the {@link Planet planets} and other celestial objects this mod offers.
 * @author GlennFolker
 */
public final class CPlanets{
    public static Planet outpost;

    private CPlanets(){
        throw new AssertionError();
    }

    /** Instantiates all contents. Called in the main thread in {@link ConficturaMod#loadContent()}. */
    public static void load(){
        outpost = new OutpostPlanet("outpost", Planets.sun, 0.3f){{
            camRadius = 0.5f;
            minZoom = 0f;

            islands = new Island[]{
                new Island(0.4f, (x, y, hex) -> {
                    float dst = Mathf.len(x, y) / 0.35f;
                    hex.low = -0.55f + dst * 0.45f + Simplex.noise2d(1, 3f, 0.5f, 4.5f, x + 31.41f, y + 59.26f) * (0.3f + Interp.pow2In.apply(dst) * 0.7f) * 0.1f;
                    hex.high = -Interp.pow3In.apply(dst) * 0.067f + Simplex.noise2d(0, 3f, 0.5f, 4f, x + 53.58f, y + 97.93f) * (0.4f + Interp.pow2In.apply(dst) * 0.6f) * 0.096f;

                    hex.lowColor.set(Pal.darkerGray).lerp(Pal.stoneGray, Interp.pow2.apply(Simplex.noise3d(2, 3f, 0.3f, 5f, x, y, hex.low)));
                    hex.highColor.set(Pal.darkerGray).lerp(Pal.stoneGray, Interp.pow2.apply(Simplex.noise3d(2, 3f, 0.3f, 5f, x, y, hex.high)));
                })
            };
        }};
    }
}
