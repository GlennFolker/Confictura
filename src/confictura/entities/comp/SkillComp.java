package confictura.entities.comp;

import confictura.entities.units.*;
import confictura.entities.units.Skill.*;
import ent.anno.Annotations.*;
import mindustry.gen.*;
import mindustry.type.*;

@EntityComponent
abstract class SkillComp implements Unitc{
    @SyncLocal SkillState[] skills;

    void setupSkills(CUnitType type){
        // If `skills` isn't null, then it is already filled from `setType(UnitType)` and synced from network code.
        if(skills != null) return;

        skills = new SkillState[type.skills.size];
        for(int i = 0; i < skills.length; i++) skills[i] = type.skills.get(i).create();
    }

    @Override
    public void setType(UnitType type){
        if(!(type instanceof CUnitType c)) throw new IllegalArgumentException("'" + type + "' isn't an instance of `CUnitType`.");
        setupSkills(c);
    }
}
