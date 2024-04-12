package confictura.io;

import arc.struct.*;
import arc.util.io.*;
import confictura.entities.units.*;
import confictura.entities.units.Skill.*;
import confictura.input.InputAggregator.*;
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
            write.str(skill.type().name);

            bytes.reset();
            out.b(skill.revision());
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
            if(skill == null || !skill.type().name.equals(name)){
                if(skill != null && skill.unit != null) skill.removed();
                skill = skills[i] = Skill.create(name);
            }

            skill.read(read, read.b());
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

    public static void writeStrings(Writes write, Seq<String> array){
        write.i(array.size);
        array.each(write::str);
    }

    public static Seq<String> readStrings(Reads read){
        int size = read.i();
        Seq<String> out = new Seq<>(size);

        for(int i = 0; i < size; i++) out.add(read.str());
        return out;
    }

    public static <T extends Enum<T>> void writeEnums(Writes write, Seq<T> array){
        write.i(array.size);
        array.each(e -> write.b(e.ordinal()));
    }

    public static <T extends Enum<T>> Seq<T> readEnums(Reads read, FromOrdinal<T> prov){
        int size = read.i();
        Seq<T> out = new Seq<>(size);

        for(int i = 0; i < size; i++) out.add(prov.get(read.b()));
        return out;
    }

    public static void writeTaps(Writes write, Seq<TapResult> array){
        writeEnums(write, array);
    }

    public static Seq<TapResult> readTaps(Reads read){
        return readEnums(read, ordinal -> TapResult.all[ordinal]);
    }

    public interface FromOrdinal<T extends Enum<T>>{
        T get(int ordinal);
    }
}
