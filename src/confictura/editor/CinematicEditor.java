package confictura.editor;

import arc.util.serialization.*;
import confictura.cinematic.*;

import java.io.*;

import static mindustry.Vars.*;

/**
 * Editor extension for {@link CinematicSector}s.
 * @author GlennFolker
 */
public class CinematicEditor extends EditorListener{
    protected CinematicSector sector;

    @Override
    public boolean shouldAttach(){
        var c = content.sector("confictura-" + editor.tags.get("name"));
        if(c instanceof CinematicSector sector){
            this.sector = sector;
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void enter(){
        String encoded;
        if((encoded = editor.tags.get("confictura-cinematic")) != null){
            try(var in = new DataInputStream(new ByteArrayInputStream(Base64Coder.decode(encoded)))){
                sector.cinematic.read(in);
            }catch(IOException e){
                ui.showException("@dialog.confictura-cinematic-read-fail", e);
            }
        }else{
            // Just in case.
            sector.cinematic.clear();
        }
    }

    @Override
    public void exit(){
        try(var stream = new ByteArrayOutputStream();
            var out = new DataOutputStream(stream)
        ){
            sector.cinematic.write(out);
            editor.tags.put("confictura-cinematic", new String(Base64Coder.encode(stream.toByteArray())));
        }catch(IOException e){
            sector.cinematic.clear();
            ui.showException("@dialog.confictura-cinematic-write-fail", e);
        }

        sector = null;
    }

    public void clear(){
        sector.cinematic.clear();
        editor.tags.remove("confictura-cinematic");
    }
}
