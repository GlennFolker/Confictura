package confictura.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.pooling.*;
import confictura.graphics.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;

import static arc.Core.*;
import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;
import static arc.math.Interp.*;
import static confictura.graphics.CPal.*;
import static confictura.util.StructUtils.*;
import static mindustry.Vars.*;

/**
 * Defines the {@linkplain Effect visual effects} this mod offers.
 * @author GlFolker
 */
public final class CFx{
    private static final Rand rand = new Rand();
    private static final Vec2 v1 = new Vec2(), v2 = new Vec2();
    private static final Color c1 = new Color();

    public static final Effect

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
    }),

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

    satelliteEntrySignal = new Effect(60f, e -> {
        e.scaled(48f, s -> {
            z(Layer.effect);
            color(monolithLighter, monolithMid, monolithDarker, s.fin() * 1.5f);
            stroke(s.fout());

            Lines.circle(e.x, e.y, s.fin(pow3Out) * 24f);
        });

        z(Layer.blockAdditive);
        blend(Blending.additive);
        color(Color.white, monolithMid, monolithDarker, e.fin(pow5In));
        alpha(e.fout(pow2Out));

        TextureRegion region = atlas.find("confictura-monolith-brand"), glow = atlas.find("confictura-monolith-brand-glow");

        float
            grow = 8f, prog = e.fin(pow3Out) * grow,
            regSize = region.width * region.scl() * xscl * 0.75f, glowSize = glow.width * region.scl() * xscl * 0.75f,
            aspect = (float)glow.width / region.width;

        regSize = regSize - grow + prog;
        glowSize = glowSize + (-grow + prog) * aspect;

        Draw.rect(region, e.x, e.y, regSize, regSize);
        Draw.rect(glow, e.x, e.y, glowSize, glowSize);

        blend();
    }).layer(Layer.blockAdditive),

    satelliteEntryTrails = new ExtEffect(() -> {
        class State extends EffectState{
            @Override
            public void remove(){
                if(data instanceof Trail[] trails) each(trails, trail -> Fx.trailFade.at(x, y, trail.width(), monolithLighter, trail));
                super.remove();
            }
        } return Pools.obtain(State.class, State::new);
    }, () -> new Trail[]{
        CTrails.singlePhantasmal(72),
        CTrails.singlePhantasmal(72)
    }, /* Lifetime is set later by `SatelliteEntry`, don't worry. */ 0f, e -> {
        if(!(e.data instanceof Trail[] trails)) return;

        for(int i = 0; i < trails.length; i++){
            v1.trns(e.fin(pow5In) * 1.5f * 360f + 90f * Mathf.signs[i], e.fout(pow10Out) * 32f).add(e.x, e.y);

            var trail = trails[i];
            if(!state.isPaused()) trail.update(v1.x, v1.y, 1f);

            trail.drawCap(monolithLighter, 1f);
            trail.draw(monolithLighter, 1f);
        }
    }),

    satelliteEntryExhaust = new Effect(60f, e -> {
        e.scaled(24f, s -> {
            z(Layer.blockAdditive);
            color(monolithLighter, monolithMid, monolithDarker, s.fin(pow2Out));
            alpha(s.fout(pow2Out));

            blend(Blending.additive);

            float size = s.fout(pow2In) * 6f;
            Draw.rect("hcircle", e.x, e.y, size, size, e.rotation - 180f);

            v1.trns(e.rotation + 90f, size / 2f);
            v2.trns(e.rotation, 32f);

            float c1 = getColor().toFloatBits(), c2 = CFx.c1.set(getColor()).a(0f).toFloatBits();
            Fill.quad(
                e.x - v1.x, e.y - v1.y, c1,
                e.x + v2.x - v1.x, e.y + v2.y - v1.y, c2,
                e.x + v2.x + v1.x, e.y + v2.y + v1.y, c2,
                e.x + v1.x, e.y + v1.y, c1
            );

            blend();
            reset();
        });

        z(Layer.effect);

        color(monolithLighter, monolithMid, monolithDarker, e.fin());
        stroke(1.5f * e.fout());
        randLenVectors(e.id, 6, 64f, e.rotation, 30f, (x, y) ->
            lineAngle(e.x + x * e.fin(pow3Out), e.y + y * e.fin(pow3Out), Mathf.angle(x, y), e.fout(pow2Out) * 8f)
        );
    });

    private CFx(){
        throw new AssertionError();
    }
}
