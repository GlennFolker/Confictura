package confictura;

import arc.*;
import confictura.assets.*;
import confictura.content.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;

/**
 * The mod's main class. Contains static references to mod class instances.
 * @author GlennFolker
 */
public class ConficturaMod extends Mod{
    public ConficturaMod(){
        Events.on(FileTreeInitEvent.class, e -> Core.app.post(CShaders::load));
    }

    @Override
    public void loadContent(){
        CTerrains.load();
        CPlanets.load();
    }
}
