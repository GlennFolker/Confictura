package confictura.ui.dialogs;

import arc.func.*;
import arc.util.*;
import confictura.ui.elements.*;
import mindustry.gen.*;
import mindustry.ui.dialogs.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class ScriptDialog extends BaseDialog{
    protected @Nullable ConsT<String, ?> validator;
    protected @Nullable Cons<String> acceptor;
    protected ScriptArea area;

    public ScriptDialog(){
        super("@dialog.confictura-cinematic-editor.scripts");

        cont.add(area = new ScriptArea("")).grow();

        buttons.defaults().size(320f, 64f);
        buttons.button("@back", Icon.left, () -> verify(this::hide));
        buttons.button("@dialog.confictura-cinematic-editor.scripts-compile", Icon.refresh, this::verify);

        hidden(() -> {
            if(acceptor != null) acceptor.get(area.getText());
            validator = null;
            acceptor = null;
        });

        keyDown(key -> {
            switch(key){
                case escape, back -> verify(() -> app.post(this::hide));
            }
        });
    }

    public void show(String source, ConsT<String, ?> validator, Cons<String> acceptor){
        area.setText(source);
        this.validator = validator;
        this.acceptor = acceptor;

        show();
    }

    public boolean verify(){
        try{
            if(validator != null) validator.get(area.getText());
            return true;
        }catch(Throwable e){
            ui.showErrorMessage(e.getLocalizedMessage());
            return false;
        }
    }

    public void verify(Runnable run){
        if(verify()) run.run();
    }
}
