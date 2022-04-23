package confictura.entities.bullets;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import confictura.graphics.*;
import confictura.util.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;

/**
 * A {@link ContinuousLaserBulletType} that updates and renders a {@link SlashTrail}.
 * @author GlennFolker
 */
public class BladeBulletType extends ContinuousLaserBulletType{
    public float trailLayer = Layer.flyingUnitLow;
    public Func<Bullet, SlashTrail> trailType = b -> new SlashTrail(
        Core.atlas.find("confictura-slash-trail"),
        trailLength
    );

    public BladeBulletType(float damage){
        super(damage);
        trailLength = 12;
    }

    @Override
    public void update(Bullet b){
        super.update(b);
        if(!(b.trail instanceof SlashTrail t)){
            b.trail = trailType.get(b);
        }else{
            float lastAngle = -(t.lastAngle() * Mathf.radDeg), currAngle = b.rotation() + 90f, dst = CMath.angleDistSigned(currAngle, lastAngle);
            if(!Mathf.zero(dst)) t.flip = dst <= 0f;

            Tmp.v1.trns(currAngle - 90f, len(b) / 2f).add(b);
            t.update(Tmp.v1.x, Tmp.v1.y, -(currAngle * Mathf.degRad), 1f);
        }
    }

    @Override
    public void draw(Bullet b){
        if(trailLength > 0 && b.trail != null){
            float z = Draw.z();
            Draw.z(trailLayer);

            b.trail.draw(trailColor, len(b) / 2f);
            Draw.z(z);
        }

        super.draw(b);
    }

    public float len(Bullet b){
        return Damage.findLaserLength(b, length) * Mathf.clamp(b.time > b.lifetime - fadeTime ? 1f - (b.time - (lifetime - fadeTime)) / fadeTime : 1f);
    }
}
