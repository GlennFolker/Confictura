package confictura.world;

import arc.*;
import arc.util.*;
import arc.util.io.*;
import confictura.*;
import confictura.ConficturaMod.*;
import confictura.util.*;
import mindustry.core.GameState.*;
import mindustry.game.EventType.*;
import mindustry.type.*;
import rhino.*;

import java.io.*;

import static mindustry.Vars.*;

/**
 * Sector presets that hooks a script when loaded.
 * @author GlennFolker
 */
public class ScriptedSector extends SectorPreset{
    public Hook hook;
    public static @Nullable Hook current;

    public ScriptedSector(String name, Planet planet, int sector){
        super(name, planet, sector);
        hook = new Hook();
    }

    public boolean canHook(){
        return
            (state.hasSector() && state.getSector() == sector) ||
            state.map != null && generator.map.file.equals(state.map.file);
    }

    protected static Object call(Function func, Object[] args){
        return func == null ? null : func.call(ScriptUtils.context, ScriptUtils.scope, func, args);
    }

    public class Hook{
        protected @Nullable Function enter, exit, update, draw, write, read, reset;
        protected final Object[]
            enterArgs = new Object[0], exitArgs = new Object[0],
            updateArgs = new Object[0], drawArgs = new Object[0],
            writeArgs = new Object[1], readArgs = new Object[1], resetArgs = new Object[0];

        private boolean bound;

        public Hook(){
            var file = tree.get("sector-scripts/confictura/" + name.substring("confictura-".length()) + ".js");
            var ret = ScriptUtils.context
                .compileFunction(ScriptUtils.scope, file.readString(), name + ".js", 1)
                .construct(ScriptUtils.context, ScriptUtils.scope, new Object[0]);

            if(!(ret instanceof NativeObject object)) throw new IllegalStateException("Sector script functions must return a JavaScript object.");
            enter = ScriptableObject.getTypedProperty(object, "enter", Function.class);
            exit = ScriptableObject.getTypedProperty(object, "exit", Function.class);
            update = ScriptableObject.getTypedProperty(object, "update", Function.class);
            draw = ScriptableObject.getTypedProperty(object, "draw", Function.class);
            write = ScriptableObject.getTypedProperty(object, "write", Function.class);
            read = ScriptableObject.getTypedProperty(object, "read", Function.class);
            reset = ScriptableObject.getTypedProperty(object, "reset", Function.class);

            if(update != null) Events.run(Trigger.update, () -> {
                if(bound) call(update, updateArgs);
            });

            if(draw != null) Events.run(Trigger.drawOver, () -> {
                if(bound) call(draw, drawArgs);
            });

            Events.on(StateChangeEvent.class, e -> {
                if(current != this && !bound && e.to == State.playing && canHook()){
                    if(current != null){
                        call(current.exit, current.exitArgs);
                        current.bound = false;
                    }

                    current = this;
                    bound = true;

                    call(reset, resetArgs);
                    try{
                        ConficturaMod.read(Chunk.scriptedSector, this::read);
                    }catch(IOException ex){
                        throw new RuntimeException(ex);
                    }

                    call(enter, enterArgs);
                }
            });

            Events.on(ResetEvent.class, e -> {
                if(current == this && bound){
                    call(exit, exitArgs);

                    current = null;
                    bound = false;
                }
            });
        }

        public void write(DataOutput stream) throws IOException{
            writeArgs[0] = Writes.get(stream);
            try{
                call(write, writeArgs);
            }catch(RuntimeException e){
                if(e.getCause() instanceof IOException io){
                    throw io;
                }else{
                    throw e;
                }
            }
        }

        public void read(DataInput stream) throws IOException{
            readArgs[0] = Reads.get(stream);
            try{
                call(read, readArgs);
            }catch(RuntimeException e){
                if(e.getCause() instanceof IOException io){
                    throw io;
                }else{
                    throw e;
                }
            }
        }
    }
}
