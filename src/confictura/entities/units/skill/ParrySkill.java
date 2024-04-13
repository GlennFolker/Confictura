package confictura.entities.units.skill;

import arc.math.geom.*;
import confictura.entities.units.*;
import confictura.gen.*;
import mindustry.content.*;
import mindustry.entities.*;

public class ParrySkill extends Skill{
    private static final Vec2 vec = new Vec2();

    public float clipSize = 16f;
    public float offset = 2.4f;

    public Effect triggerEffect = Fx.none;

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
        @Override
        public void activate(float x, float y){
            float rot = unit.angleTo(x, y);
            vec.trns(rot, unit.hitSize + offset).add(unit);

            var parry = Parry.create();
            parry.skill = ParrySkill.this;
            parry.x = vec.x;
            parry.y = vec.y;
            parry.rotation = rot;
            parry.add();

            unit.rotation = rot;
            unit.snapInterpolation();

            triggerEffect.at(unit.x, unit.y, unit.rotation, unit);
        }
    }
}
