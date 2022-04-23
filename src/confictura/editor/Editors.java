package confictura.editor;

import arc.*;
import arc.struct.*;
import confictura.world.*;
import mindustry.core.GameState.*;
import mindustry.game.EventType.*;
import mindustry.io.*;

import static mindustry.Vars.*;

public final class Editors{
    public static Seq<EditorListener> listeners = new Seq<>();
    public static CollapserEditor collapser;

    private Editors(){
        throw new AssertionError();
    }

    public static void configure(){
        listeners.add(collapser = new CollapserEditor());
    }

    public static abstract class EditorListener{
        private boolean attached;

        public EditorListener(){
            Events.on(ClientLoadEvent.class, e -> register());
        }

        public void register(){
            Events.run(Trigger.update, () -> valid(this::update));
            Events.run(Trigger.drawOver, () -> valid(this::draw));
            Events.on(StateChangeEvent.class, e -> {
                if(e.from == State.menu && e.to == State.playing && state.isEditor()){
                    attached = true;
                    begin();
                }else if(attached && e.to == State.menu){
                    end();
                    attached = false;
                }
            });
        }

        public void begin(){}

        public void end(){}

        public void update(){}

        public void draw(){}

        public void valid(Runnable run){
            if(attached && state.isEditor()) run.run();
        }

        public boolean isAttached(){
            return attached;
        }

        public static StringMap data(){
            return JsonIO.json.fromJson(StringMap.class, String.class, editor.tags.get(WorldState.dataKey, "{}"));
        }

        public static String data(ObjectMap<String, String> data){
            return JsonIO.json.toJson(data, StringMap.class, String.class);
        }
    }
}
