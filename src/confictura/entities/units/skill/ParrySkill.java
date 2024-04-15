package confictura.entities.units.skill;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import confictura.content.*;
import confictura.entities.effect.*;
import confictura.entities.units.*;
import confictura.gen.*;
import mindustry.entities.*;
import mindustry.gen.*;

public class ParrySkill extends Skill{
    private static final Vec2 vec = new Vec2();

    public float clipSize = 16f, offset = 6f;
    public float impact = 0.85f, impactTime = 48f;
    public Interp impactInterp = Interp.pow3Out;

    public Effect triggerEffect = CFx.parry;

    public ParrySkill(String name){
        super(name);
    }

    public void update(Parry parry){

    }

    public void draw(Parry parry){

    }

    @Override
    public ParryState create(){
        return new ParryState();
    }

    public class ParryState extends SkillState{
        protected float last = Float.NEGATIVE_INFINITY;

        @Override
        public void activate(float x, float y){
            last = Time.time;
            float lastRot = Mathf.mod(unit.rotation, 360f);

            float rot = Mathf.mod(unit.angleTo(x, y), 360f);
            vec.trns(rot, unit.hitSize + offset).add(unit);

            var parry = Parry.create();
            parry.skill = ParrySkill.this;
            parry.x = vec.x;
            parry.y = vec.y;
            parry.rotation = rot;
            parry.clockwise = lastRot > rot == Angles.backwardDistance(lastRot, rot) > Angles.forwardDistance(lastRot, rot);
            parry.add();

            unit.rotation = rot;
            unit.snapInterpolation();

            var data = FxData.create();
            data.delegate = unit;
            data.data = new ParryData(){{
                fromRot = lastRot;
                toRot = rot;
                offset = ParrySkill.this.offset;
                unit = ParryState.this.unit;
                clockwise = parry.clockwise;
            }};

            triggerEffect.at(unit.x, unit.y, 0f, data);
        }

        @Override
        public float speedMultiplier(){
            return 1f - impactInterp.apply(1f - Math.min(Time.time - last, impactTime) / impactTime) * impact;
        }

        @Override
        public float reloadMultiplier(){
            return 1f - impactInterp.apply(1f - Math.min(Time.time - last, impactTime) / impactTime) * impact;
        }
    }

    public static class ParryData{
        public float fromRot, toRot, offset;
        public boolean clockwise;
        public Unit unit;
    }
}
