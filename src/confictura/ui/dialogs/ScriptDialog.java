package confictura.ui.dialogs;

import confictura.ui.elements.*;
import mindustry.ui.dialogs.*;

public class ScriptDialog extends BaseDialog{
    protected ScriptArea area;

    public ScriptDialog(){
        super("@dialog.confictura-cinematic-editor.scripts");

        cont.add(area = new ScriptArea("")).grow();

        addCloseButton(320f);
    }
}
