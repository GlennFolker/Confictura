package confictura.input;

import arc.*;
import arc.func.*;
import arc.input.*;
import arc.input.GestureDetector.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.struct.*;
import arc.util.*;
import arc.util.pooling.*;
import confictura.net.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class InputAggregator implements Eachable<String>{
    protected final OrderedMap<String, TapHandle> handles;
    protected final Seq<String> handleKeys;
    protected final Seq<TapResult> results = new Seq<>();

    protected final Seq<Vec2> taps = new Seq<>();
    protected final Pool<Vec2> tapPool = new Pool<>(){
        @Override
        protected Vec2 newObject(){
            return new Vec2();
        }
    };

    public InputAggregator(){
        // Store as an ordered map for fast iteration, but don't actually order the keys for fast swap-remove.
        handles = new OrderedMap<>();
        (handleKeys = handles.orderedKeys()).ordered = false;

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
                        taps.add(tapPool.obtain().set(x, y));
                        return true;
                    }else{
                        return false;
                    }
                }
            }));

            Events.run(Trigger.update, () -> {
                //TODO `Keybinds` should have a way to add default bindings.
                if(!mobile && input.keyTap(KeyCode.altLeft) && !(scene.getKeyboardFocus() instanceof TextField)){
                    taps.add(tapPool.obtain().set(input.mouseWorld()));
                }

                for(var handle : handles.values()) handle.enabled = handle.predicate.get();
                if(taps.any()){
                    taps.each(tap -> CCall.tap(player, tap.x, tap.y, handleKeys));

                    tapPool.freeAll(taps);
                    taps.clear();
                }
            });
        }
    }

    public TapHandle onTap(String key, TapListener listener){
        if(handles.containsKey(key)) throw new IllegalArgumentException("Tap listener '" + key + "' already added.");
        var handle = new TapHandle();
        handle.index = handleKeys.size;
        handle.listener = listener;

        handles.put(key, handle);
        return handle;
    }

    public Seq<TapResult> tryTap(Player player, float x, float y, Seq<String> targets){
        results.clear();
        for(int i = 0; i < targets.size; i++){
            var handle = handles.get(targets.get(i));
            results.add((handle == null || !handle.enabled) ? TapResult.disabled : handle.listener.canTap(player, x, y) ? TapResult.accepted : TapResult.rejected);
        }

        return results;
    }

    public void tap(Player player, float x, float y, Seq<String> targets, Seq<TapResult> results){
        for(int i = 0; i < targets.size; i++){
            var handle = handles.get(targets.get(i));
            var result = results.get(i);
            if(handle != null && result != TapResult.disabled) handle.listener.tapped(player, x, y, result == TapResult.accepted);
        }
    }

    @Override
    public void each(Cons<? super String> listener){
        handleKeys.each(listener);
    }

    public enum TapResult{
        accepted, rejected, disabled;

        public static final TapResult[] all = values();
    }

    public class TapHandle{
        protected int index;
        protected TapListener listener;

        protected boolean enabled = true, removed = false;
        protected Boolp predicate = () -> true;

        public void enabled(Boolp predicate){
            this.predicate = predicate;
        }

        public void remove(){
            if(removed) return;
            removed = true;

            // Swap-remove, and fix the swapped handle's index.
            handles.removeIndex(index);
            if(handleKeys.size > index) handles.get(handleKeys.get(index)).index = index;
        }
    }

    public interface TapListener{
        default boolean canTap(Player player, float x, float y){
            return true;
        }

        void tapped(Player player, float x, float y, boolean accepted);
    }
}
