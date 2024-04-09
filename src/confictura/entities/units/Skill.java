package confictura.entities.units;

import arc.struct.*;
import arc.util.io.*;

public abstract class Skill{
    protected static final ObjectMap<String, Skill> all = new ObjectMap<>();

    public final String name;

    public Skill(String name){
        this.name = name;
        if(all.put(name, this) != null) throw new IllegalArgumentException("Skill '" + name + "' already exists.");
    }

    public static SkillState create(String name){
        return all.getThrow(name, () -> new IllegalArgumentException("Skill '" + name + "' not found.")).create();
    }

    public abstract SkillState create();

    public abstract class SkillState{
        public String name(){
            return name;
        }

        public abstract void write(Writes write);

        public abstract void read(Reads read);
    }
}
