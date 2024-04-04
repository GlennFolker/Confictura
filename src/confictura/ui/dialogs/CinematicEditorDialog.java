package confictura.ui.dialogs;

import arc.scene.ui.TextButton.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import static confictura.ConficturaMod.*;
import static mindustry.Vars.*;

public class CinematicEditorDialog extends BaseDialog{
    public CinematicEditorDialog(){
        super("@dialog.confictura-cinematic-editor");

        cont.clear();
        if(!mobile){
            cont.defaults().width(320f).height(55).pad(5f);
            cont.button("@dialog.confictura-cinematic-editor.scripts", Icon.pencil, () -> {
                scriptDialog.show(cinematic.script.getSource(), cinematic.script::compile, null);
                hide();
            }).row();
            cont.button("@quit", Icon.exit, this::hide);

            ui.hudGroup.fill(cont -> cont.bottom().left().button("@dialog.confictura-cinematic-editor", Icon.pencil, new TextButtonStyle(){{
                font = Fonts.def;
                up = Tex.buttonEdge3;
                down = Tex.buttonEdgeDown3;
                over = Tex.buttonEdgeOver3;
            }}, this::show).width(320f).visible(() -> ui.hudfrag.shown && cinematicEditor.isAttached()));
        }else{
            //TODO mobile.
        }

        addCloseListener();
    }
}
