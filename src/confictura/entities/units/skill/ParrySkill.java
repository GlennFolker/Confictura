package confictura.entities.units.skill;

import arc.util.io.*;
import confictura.entities.units.*;

public class ParrySkill extends Skill{
    public ParrySkill(String name){
        super(name);
    }

    @Override
    public SkillState create(){
        return null;
    }

    public class ParryState extends SkillState{
        @Override
        public void write(Writes write){
            write.b(0);
        }

        @Override
        public void read(Reads read){
            byte rev = read.b();
            switch(rev){
                case 0 -> {}
                default -> throw new IllegalArgumentException("Unknown revision " + rev + ".");
            }
        }
    }
}
