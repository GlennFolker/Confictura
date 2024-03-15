package confictura.cinematic;

import confictura.util.*;
import rhino.*;

import java.io.*;

public class Script{
    protected String source;

    protected ImporterTopLevel scope;

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

            var function = ScriptUtils.context.compileFunction(scope, source, "confictura-cinematic.js", 0);
            var result = function.construct(ScriptUtils.context, function, new Object[0]);
            if(!(result instanceof NativeObject)) throw new IllegalArgumentException("Sector script must be a constructor function!");
        }catch(Throwable e){
            compile("function(){}", false);
            throw e;
        }
    }

    public void clear(){
        try{
            compile("function(){}", true);
        }catch(Throwable e){
            throw new RuntimeException(e);
        }
    }

    public void write(DataOutput stream) throws IOException{
        stream.writeShort(0);
        stream.writeUTF(source);
    }

    public void read(DataInput stream) throws IOException{
        short version = stream.readShort();
        switch(version){
            case 0 -> {
                try{
                    compile(stream.readUTF());
                }catch(Throwable e){
                    throw new IOException("Failed to compile sector script.", e);
                }
            }
            default -> throw new IOException("Unknown revision " + version + ".");
        }
    }
}
