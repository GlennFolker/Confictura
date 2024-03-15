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
import static mindustry.Vars.*;

public class Cinematic{
    protected final ReusableByteOutStream out = new ReusableByteOutStream();
    protected final ReusableByteInStream in = new ReusableByteInStream();

    private boolean attached;
    private SectorPreset sector;

    public Script script = new Script();

    public Cinematic(){
        Events.on(SaveWriteEvent.class, e -> {
            if(attached){
                try{
                    writeTo(state.rules.tags);
                }catch(IOException ex){
                    throw new RuntimeException(ex);
                }
            }
        });

        Events.on(SaveLoadEvent.class, e -> {
            try{
                readFrom(state.rules.tags);
            }catch(IOException ex){
                throw new RuntimeException(ex);
            }
        });

        Events.on(StateChangeEvent.class, e -> {
            var sect = state.hasSector()
                ?   state.getSector().preset
                :   (state.map != null && isConfictura(state.map.mod))
                    ?   content.sector(state.map.name())
                    :   null;

            if(sect != null){
                if(!attached || sector != sect){
                    sector = sect;
                    attached = true;
                }
            }else if(attached || e.to == State.menu){
                attached = false;
            }
        });
    }

    public SectorPreset sector(){
        return sector;
    }

    public void clear(){
        script.clear();
    }

    public void writeTo(StringMap tags) throws IOException{
        out.reset();

        var stream = new DataOutputStream(out);
        write(stream);

        tags.put("confictura-cinematic", new String(Base64Coder.encode(out.getBytes(), out.size())));
    }

    public void readFrom(StringMap tags) throws IOException{
        clear();

        String buf;
        if((buf = tags.get("confictura-cinematic")) != null){
            in.setBytes(Base64Coder.decode(buf));
            read(new DataInputStream(in));
        }
    }

    public void write(DataOutput stream) throws IOException{
        stream.writeShort(1);
        script.write(stream);
    }

    public void read(DataInput stream) throws IOException{
        short version = stream.readShort();
        switch(version){
            case 0 -> script.clear();
            case 1 -> script.read(stream);
            default -> throw new IOException("Unknown revision " + version + ".");
        }
    }
}
