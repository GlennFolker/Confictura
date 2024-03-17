package confictura.content;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import mindustry.entities.*;

import static arc.graphics.g2d.Draw.*;
import static arc.math.Interp.*;
import static confictura.graphics.CPal.*;

/**
 * Defines the {@linkplain Effect visual effects} this mod offers.
 * @author GlennFolker
 */
public final class CFx{
    private static final Rand rand = new Rand();
    private static final Vec2 v1 = new Vec2();

    public static final Effect

    erodedEneraphyteSteam = new Effect(100f, e -> {
        color(monolithLight, monolithDark, monolithDarker, e.fin(smoother));
        alpha(e.fslope() * 0.5f);

        float len = 1f + e.finpow() * 4f;
        rand.setSeed(e.id);

        int amount = rand.random(1, 3);
        for(int i = 0; i < amount; i++){
            v1.trns(rand.random(360f), rand.random(len)).add(e.x, e.y);
            Fill.circle(v1.x, v1.y, rand.random(0.6f, 1.7f) + smooth.apply(e.fslope()) * 0.75f);
        }
    }),

    eneraphyteSteam = new Effect(120f, e -> {
        color(monolithLighter, monolithMid, monolithDark, e.fin(smoother));
        alpha(e.fslope() * 0.7f);

        float len = 1f + e.finpow() * 5f;
        rand.setSeed(e.id);

        int amount = rand.random(1, 3);
        for(int i = 0; i < amount; i++){
            v1.trns(rand.random(360f), rand.random(len)).add(e.x, e.y);
            Fill.circle(v1.x, v1.y, rand.random(0.8f, 2f) + smooth.apply(e.fslope()) * 0.9f);
        }
    });

    private CFx(){
        throw new AssertionError();
    }
}
