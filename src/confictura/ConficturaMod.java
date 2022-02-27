package confictura;

import arc.*;
import arc.graphics.*;
import confictura.assets.*;
import confictura.content.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;

import static mindustry.Vars.*;

/**
 * The mod's main class. Contains static references to mod class instances.
 * @author GlennFolker
 */
public class ConficturaMod extends Mod{
    public ConficturaMod(){
        Events.on(FileTreeInitEvent.class, e -> Core.app.post(CShaders::load));
        Events.on(ClientLoadEvent.class, e -> mods.getScripts().runConsole("""
            importPackage(java.lang);
            let bindTrail = (len, col, z) => {
                let trail = Class.forName("confictura.graphics.SlashTrail", true, Vars.mods.mainLoader()).getConstructor(TextureRegion, Integer.TYPE).newInstance(Core.atlas.find("confictura-slash-trail"), new Integer(len));
                Events.run(Trigger.update, () => { if(!Vars.state.isPaused()) trail.update(Vars.player.x, Vars.player.y); });
                Events.run(Trigger.drawOver, () => { Draw.z(z); trail.draw(col, 20); });

                return trail;
            }
            """
        ));
    }

    @Override
    public void loadContent(){
        CTerrains.load();
        CPlanets.load();
    }

    /** Throws an exception if OpenGL encountered an error. */
    public static void checkGLError(){
        int err = Gl.getError();
        if(err != Gl.noError) throw new IllegalStateException("OpenGL error: " + switch(err){
            case Gl.invalidEnum -> "Invalid enum.";
            case Gl.invalidValue -> "Invalid value.";
            case Gl.invalidOperation -> "Invalid operation.";
            case Gl.outOfMemory -> "Out of memory.";
            default -> "Unknown error.";
        });
    }
}
