package confictura;

import arc.util.*;
import confictura.content.*;
import confictura.gen.*;
import mindustry.*;
import mindustry.mod.*;

import static mindustry.Vars.*;

/**
 * Main entry point of the mod. Handles startup things like content loading, entity registering, other utility
 * attaching, and more. Stores static references to modules defined by this mod, similar to what {@link Vars} does.
 * @author GlennFolker
 */
@SuppressWarnings("unchecked")
public class ConficturaMod extends Mod{
    public static DevBuild dev;

    public ConficturaMod(){
        try{
            Class<? extends DevBuild> devImpl = (Class<? extends DevBuild>)Class.forName("confictura.DevBuildImpl", true, mods.mainLoader());
            dev = devImpl.getConstructor().newInstance();

            Log.info("[Confictura] Instantiated developer build.");
        }catch(ClassNotFoundException | NoClassDefFoundError e){
            dev = new DevBuild(){};
            Log.info("[Confictura] Instantiated user build.");
        }catch(Throwable e){
            Log.err("[Confictura] Failed instantiating developer build", Strings.getFinalCause(e));
        }
    }

    @Override
    public void loadContent(){
        EntityRegistry.register();
        CPlanets.load();
    }
}
