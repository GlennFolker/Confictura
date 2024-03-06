package confictura.editor;

import arc.*;
import mindustry.core.GameState.*;
import mindustry.game.EventType.*;

import static mindustry.Vars.*;

/**
 * Base class for listeners that attach themselves when the game is in editor mode.
 * @author GlennFolker
 */
public abstract class EditorListener{
    private boolean attached;

    protected EditorListener(){
        Events.run(Trigger.update, () -> valid(this::update));
        Events.run(Trigger.drawOver, () -> valid(this::draw));
        Events.on(StateChangeEvent.class, e -> {
            if(e.from == State.menu && e.to == State.playing && state.isEditor()){
                if(shouldAttach()){
                    attached = true;
                    enter();
                }
            }else if(attached && e.to == State.menu){
                exit();
                attached = false;
            }
        });
    }

    public abstract boolean shouldAttach();

    public void enter(){}

    public void exit(){}

    public void update(){}

    public void draw(){}

    public void valid(Runnable run){
        if(attached) run.run();
    }
}
