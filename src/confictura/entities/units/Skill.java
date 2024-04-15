package confictura.entities.units;

import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import confictura.input.InputAggregator.*;
import mindustry.gen.*;

import static confictura.ConficturaMod.*;

public abstract class Skill{
    protected static final ObjectMap<String, Skill> all = new ObjectMap<>();

    public final String name;
    public float reloadTime = 60f;

    public Skill(String name){
        this.name = name;
        if(all.put(name, this) != null) throw new IllegalArgumentException("Skill '" + name + "' already exists.");
    }

    public static SkillState create(String name){
        return all.getThrow(name, () -> new IllegalArgumentException("Skill '" + name + "' not found.")).create();
    }

    public abstract SkillState create();

    public abstract class SkillState implements TapListener{
        public TapHandle tap;
        public Unit unit;

        public float reload = reloadTime;

        public Skill type(){
            return Skill.this;
        }

        public boolean interactive(){
            return unit.isLocal();
        }

        public void added(){
            (tap = inputAggregator.onTap(name + "#" + unit.id, this)).enabled(this::interactive);
        }

        public void removed(){
            tap.remove();
        }

        public void update(){
            updateState();
            if(!interactive()) updatePassive();
        }

        public void updateState(){
            reload = Math.min(reload + Time.delta, reloadTime);
        }

        public void updatePassive(){}

        public void draw(){}

        public float speedMultiplier(){
            return 1f;
        }

        public float reloadMultiplier(){
            return 1f;
        }

        @Override
        public boolean canTap(Player player, float x, float y){
            return reload >= reloadTime;
        }

        @Override
        public void tapped(Player player, float x, float y, boolean accepted){
            if(accepted){
                activate(x, y);
                reload = 0f;
            }else{
                regress(x, y);
            }
        }

        public void regress(float x, float y){}

        public void activate(float x, float y){}

        public void write(Writes write){}

        public void read(Reads read, byte revision){}

        public byte revision(){
            return 0;
        }
    }
}
