package confictura.entities.units;

import arc.struct.*;
import confictura.gen.*;
import mindustry.gen.*;
import mindustry.type.*;

public class CUnitType extends UnitType{
    public Seq<Skill> skills = new Seq<>();

    @SuppressWarnings("unchecked")
    public <T extends Unit> CUnitType(String name, Class<T> type){
        super(name);
        constructor = EntityRegistry.content(name, type, n -> EntityMapping.map(this.name));
        if(constructor == null) throw new IllegalArgumentException("Unit entity class `" + type + "` not registered.");
    }
}
