package confictura.entities.comp;

import confictura.entities.units.skill.*;
import confictura.gen.*;
import ent.anno.Annotations.*;
import mindustry.gen.*;

@EntityComponent(base = true)
@EntityDef(value = Parryc.class, serialize = false, pooled = true)
abstract class ParryComp implements Drawc, Rotc, Timedc{
    ParrySkill skill;
    boolean clockwise;

    @Override
    public void update(){
        skill.update(self());
    }

    @Override
    public void draw(){
        skill.draw(self());
    }

    @Override
    @Replace
    public float clipSize(){
        return skill.clipSize;
    }
}
