package confictura;

import confictura.gen.*;
import mindustry.*;
import mindustry.mod.*;

/**
 * Main entry point of the mod. Handles startup things like content loading, entity registering, other utility
 * attaching, and more. Stores static references to modules defined by this mod, similar to what {@link Vars} does.
 * @author GlennFolker
 */
public class ConficturaMod extends Mod{
    @Override
    public void loadContent(){
        EntityRegistry.register();
    }
}
