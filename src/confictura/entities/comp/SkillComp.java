package confictura.entities.comp;

import confictura.entities.units.*;
import confictura.entities.units.Skill.*;
import ent.anno.Annotations.*;
import mindustry.gen.*;
import mindustry.type.*;

@EntityComponent
abstract class SkillComp implements Unitc{
    /** Compared by-reference to see if the unit hasn't setup its skills. */
    private static final SkillState[] noSkills = {};

    @SyncLocal SkillState[] skills = noSkills;

    void setupSkills(CUnitType type){
        // If `skills` isn't null, then it is already filled from `setType(UnitType)` and synced from network code.
        if(skills == null || skills == noSkills){
            skills = new SkillState[type.skills.size];
            for(int i = 0; i < skills.length; i++) skills[i] = type.skills.get(i).create();
        }

        if(isAdded()) attachSkills();
    }

    void attachSkills(){
        for(var skill : skills){
            if(skill.unit == null){
                skill.unit = self();
                skill.added();
            }
        }
    }

    @Override
    public void add(){
        attachSkills();
    }

    @Override
    public void remove(){
        for(var skill : skills){
            if(skill.unit != null){
                skill.removed();
                skill.unit = null;
            }
        }
    }

    @Override
    public void update(){
        for(var skill : skills) skill.update();
    }

    @Override
    public void draw(){
        for(var skill : skills) skill.draw();
    }

    @Override
    public void setType(UnitType type){
        if(!(type instanceof CUnitType c)) throw new IllegalArgumentException("'" + type + "' isn't an instance of `CUnitType`.");
        setupSkills(c);
    }
}
