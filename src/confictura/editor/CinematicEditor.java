package confictura.editor;

import mindustry.type.*;

import java.io.*;

import static confictura.ConficturaMod.*;
import static mindustry.Vars.*;

/**
 * Editor extension for custom {@link SectorPreset}s.
 * @author GlennFolker
 */
public class CinematicEditor extends EditorListener{
    @Override
    public boolean shouldAttach(){
        return content.sector("confictura-" + editor.tags.get("name")) != null;
    }

    @Override
    public void enter(){
        try{
            cinematic.readFrom(editor.tags);
        }catch(IOException e){
            cinematic.clear();
            ui.showException("@dialog.confictura-cinematic-read-fail", e);
        }
    }

    @Override
    public void exit(){
        try{
            cinematic.writeTo(editor.tags);
        }catch(IOException e){
            ui.showException("@dialog.confictura-cinematic-write-fail", e);
        }finally{
            cinematic.clear();
        }
    }
}
