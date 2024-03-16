package confictura.cinematic;

import arc.*;
import arc.struct.*;
import arc.util.io.*;
import arc.util.serialization.*;
import mindustry.core.GameState.*;
import mindustry.game.EventType.*;
import mindustry.type.*;

import java.io.*;

import static confictura.ConficturaMod.*;
import static confictura.util.IOUtils.*;
import static mindustry.Vars.*;

public class Cinematic{
    protected final ReusableByteOutStream out = new ReusableByteOutStream();
    protected final ReusableByteInStream in = new ReusableByteInStream();

    private SectorPreset sector;

    public Script script = new Script();

    public Cinematic(){
        Events.run(Trigger.update, () -> attached(this::update));
        Events.run(Trigger.drawOver, () -> attached(this::draw));

        Events.on(SaveWriteEvent.class, e -> attached(() -> ioUnchecked(() -> writeStateTo(state.rules.tags))));
        Events.on(SaveLoadEvent.class, e -> ioUnchecked(() -> {
            if(readMapFrom(state.map.tags)){
                readStateFrom(state.rules.tags);
            }else{
                clear();
            }
        }));

        Events.on(StateChangeEvent.class, e -> {
            var target = state.hasSector()
                ?   state.getSector().preset
                :   (state.map != null && isConfictura(state.map.mod))
                    ?   content.sector(state.map.name())
                    :   null;

            if(target != null){
                if(sector != target){
                    if(sector != null) exit();
                    sector = target;
                    enter();
                }
            }else if(sector != null || e.to == State.menu){
                if(sector != null) exit();
                sector = null;
            }
        });
    }

    public boolean isAttached(){
        return sector != null;
    }

    public void attached(Runnable run){
        if(isAttached()) run.run();
    }

    public SectorPreset sector(){
        return sector;
    }

    public void enter(){
        script.enter();
    }

    public void exit(){
        script.exit();
    }

    public void update(){
        script.update();
    }

    public void draw(){
        script.draw();
    }

    public void clear(){
        script.clear();
    }

    protected DataOutput output(){
        out.reset();
        return new DataOutputStream(out);
    }

    protected DataInput input(String input){
        in.setBytes(Base64Coder.decode(input));
        return new DataInputStream(in);
    }

    protected String asString(){
        return new String(Base64Coder.encode(out.getBytes(), out.size()));
    }

    public void writeMapTo(StringMap tags) throws IOException{
        writeMap(output());
        tags.put("confictura-cinematic", asString());
    }

    public boolean readMapFrom(StringMap tags) throws IOException{
        String in;
        if((in = tags.get("confictura-cinematic")) != null){
            readMap(input(in));
            return true;
        }else{
            return false;
        }
    }

    public void writeStateTo(StringMap tags) throws IOException{
        writeState(output());
        tags.put("confictura-cinematic", asString());
    }

    public boolean readStateFrom(StringMap tags) throws IOException{
        String in;
        if((in = tags.get("confictura-cinematic")) != null){
            readState(input(in));
            return true;
        }else{
            return false;
        }
    }

    public void writeMap(DataOutput stream) throws IOException{
        stream.writeShort(1);
        script.writeMap(stream);
    }

    public void readMap(DataInput stream) throws IOException{
        short version = stream.readShort();
        switch(version){
            case 0 -> script.clear();
            case 1 -> script.readMap(stream);
            default -> throw new IOException("Unknown revision " + version + ".");
        }
    }

    public void writeState(DataOutput stream) throws IOException{
        stream.writeShort(0);
        script.writeState(stream);
    }

    public void readState(DataInput stream) throws IOException{
        short version = stream.readShort();
        switch(version){
            case 0 -> script.readState(stream);
            default -> throw new IOException("Unknown revision " + version + ".");
        }
    }
}
