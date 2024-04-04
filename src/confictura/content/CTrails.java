package confictura.content;

import arc.graphics.*;
import arc.math.*;
import confictura.graphics.g2d.*;
import confictura.graphics.g2d.BaseTrail.*;

public final class CTrails{
    private CTrails(){
        throw new AssertionError();
    }

    public static TexturedTrail singlePhantasmal(int length, TrailAttrib... attributes){
        return new TexturedTrail(length, "confictura-phantasmal-trail", attributes){{
            blend = Blending.additive;
            fadeInterp = Interp.pow2In;
            sideFadeInterp = Interp.pow3In;
            mixInterp = Interp.pow10In;
            gradientInterp = Interp.pow2Out;
            fadeColor = new Color(0.3f, 0.5f, 1f);
            shrink = 0f;
            fadeAlpha = 1f;
            mixAlpha = 1f;
            //trailChance = 0.4f;
            //trailWidth = 1.6f;
            //trailColor = monolithLight;
        }};
    }
}
