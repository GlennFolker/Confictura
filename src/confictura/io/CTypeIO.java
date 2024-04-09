package confictura.io;

import arc.util.io.*;
import confictura.entities.units.*;
import confictura.entities.units.Skill.*;
import ent.anno.Annotations.*;

import java.io.*;

import static confictura.util.StructUtils.*;

@TypeIOHandler
public final class CTypeIO{
    private static final ReusableByteOutStream bytes = new ReusableByteOutStream();
    private static final Writes out = new Writes(new DataOutputStream(bytes));

    private CTypeIO(){
        throw new AssertionError();
    }

    public static void writeSkills(Writes write, SkillState[] skills){
        write.s(skills.length);
        for(var skill : skills){
            write.str(skill.name());

            bytes.reset();
            skill.write(out);

            int len = bytes.size();
            write.s(len);
            write.b(bytes.getBytes(), 0, len);
        }
    }

    public static SkillState[] readSkills(Reads read, SkillState[] skills){
        int len = read.s();
        if(skills.length != len) skills = resize(skills, SkillState[]::new, len, null);

        for(int i = 0; i < len; i++){
            var name = read.str();
            read.s();

            var skill = skills[i];
            if(skill == null || !skill.name().equals(name)) skill = skills[i] = Skill.create(name);
            skill.read(read);
        }

        return skills;
    }

    public static SkillState[] readSkills(Reads read){
        for(int i = 0, len = read.s(); i < len; i++){
            read.str();
            read.skip(read.s());
        }

        return emptyArray();
    }
}
