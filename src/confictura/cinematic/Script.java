package confictura.cinematic;

import arc.func.*;
import arc.util.*;
import confictura.util.*;
import rhino.*;

import java.io.*;

import static confictura.util.StructUtils.*;

public class Script{
    protected String source;

    protected ImporterTopLevel scope;
    protected @Nullable Runnable enter, exit, update, draw;
    protected @Nullable ConsT<DataOutput, IOException> write;
    protected @Nullable ConsT<DataInput, IOException> read;

    public Script(){
        clear();
    }

    public String getSource(){
        return source;
    }

    public void compile(String source){
        compile(source, true);
    }

    protected void compile(String source, boolean set){
        try{
            if(set) this.source = source;
            scope = ScriptUtils.newScope();
            ScriptUtils.importDefaults(scope);

            var ctx = ScriptUtils.context;
            var function = ctx.compileFunction(scope, source, "confictura-cinematic.js", 0);
            var result = function.construct(ctx, function, new Object[0]);

            if(!(result instanceof NativeObject object)) throw new IllegalArgumentException("Sector script must be a constructor function!");
            var noArgs = emptyArray();

            if(object.has("enter", object)){
                if(!(object.get("enter", object) instanceof Function func)) throw new IllegalArgumentException("`enter` must be a function!");
                enter = () -> func.call(ctx, scope, func, noArgs);
            }else{
                enter = null;
            }

            if(object.has("exit", object)){
                if(!(object.get("exit", object) instanceof Function func)) throw new IllegalArgumentException("`exit` must be a function!");
                exit = () -> func.call(ctx, scope, func, noArgs);
            }else{
                exit = null;
            }

            if(object.has("update", object)){
                if(!(object.get("update", object) instanceof Function func)) throw new IllegalArgumentException("`update` must be a function!");
                update = () -> func.call(ctx, scope, func, noArgs);
            }else{
                update = null;
            }

            if(object.has("draw", object)){
                if(!(object.get("draw", object) instanceof Function func)) throw new IllegalArgumentException("`draw` must be a function!");
                draw = () -> func.call(ctx, scope, func, noArgs);
            }else{
                draw = null;
            }

            if(object.has("write", object)){
                if(!(object.get("write", object) instanceof Function func)) throw new IllegalArgumentException("`write` must be a function!");

                var args = new DataOutput[1];
                write = output -> {
                    args[0] = output;
                    func.call(ctx, scope, func, args);
                };
            }else{
                write = null;
            }

            if(object.has("read", object)){
                if(!(object.get("read", object) instanceof Function func)) throw new IllegalArgumentException("`read` must be a function!");

                var args = new DataInput[1];
                read = input -> {
                    args[0] = input;
                    func.call(ctx, scope, func, args);
                };
            }else{
                read = null;
            }
        }catch(Throwable e){
            compile("function(){}", false);
            throw e;
        }
    }

    public void clear(){
        compile("function(){}", true);
    }

    public void enter(){
        if(enter != null) enter.run();
    }

    public void exit(){
        if(exit != null) exit.run();
    }

    public void update(){
        if(update != null) update.run();
    }

    public void draw(){
        if(draw != null) draw.run();
    }

    public void writeMap(DataOutput stream) throws IOException{
        stream.writeShort(1);
        stream.writeUTF(source);
    }

    public void readMap(DataInput stream) throws IOException{
        short version = stream.readShort();
        switch(version){
            case 0, 1 -> IOUtils.ioErr(() -> compile(stream.readUTF()), "Failed to compile sector script.");
            default -> throw new IOException("Unknown revision " + version + ".");
        }
    }

    public void writeState(DataOutput stream) throws IOException{
        if(write != null) write.get(stream);
    }

    public void readState(DataInput stream) throws IOException{
        if(read != null) read.get(stream);
    }
}
