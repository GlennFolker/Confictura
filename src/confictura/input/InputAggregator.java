package confictura.input;

import arc.*;
import arc.input.*;
import arc.input.GestureDetector.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.struct.*;
import confictura.net.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class InputAggregator implements GestureListener{
    protected final Seq<TapHandle> handles = new Seq<>(false);

    protected boolean tapped;
    protected final Vec2 tap = new Vec2();

    public InputAggregator(){
        if(!headless){
            if(mobile) input.addProcessor(new GestureDetector(new GestureListener(){
                @Override
                public boolean tap(float x, float y, int count, KeyCode button){
                    if(count == 2 && !(
                        state.isMenu() ||
                        scene.hasMouse(x, y) ||
                        control.input.isPlacing() ||
                        control.input.isBreaking() ||
                        control.input.selectedUnit() != null
                    )){
                        tapped = true;
                        tap.x = x;
                        tap.y = y;

                        return true;
                    }else{
                        return false;
                    }
                }
            }));

            Events.run(Trigger.update, () -> {
                if(!mobile){
                    //TODO `Keybinds` should have a way to add default bindings.
                    if(input.keyTap(KeyCode.altLeft) && !(scene.getKeyboardFocus() instanceof TextField)){
                        tapped = true;
                        tap.set(input.mouseWorld());
                    }
                }

                if(tapped){
                    CCall.tap(player, tap.x, tap.y);
                    tapped = false;
                }
            });
        }
    }

    public TapHandle onTap(TapListener listener){
        var handle = new TapHandle(handles.size, listener);
        handles.add(handle);
        return handle;
    }

    public void tap(Player player, float x, float y){
        handles.each(handle -> handle.listener.tapped(player, x, y));
    }

    public interface TapListener{
        void tapped(Player player, float x, float y);
    }

    public class TapHandle{
        protected int index;
        protected TapListener listener;

        protected TapHandle(int index, TapListener listener){
            this.index = index;
            this.listener = listener;
        }

        public void remove(){
            handles.remove(index);
            if(handles.size > index) handles.get(index).index = index;
        }
    }
}
