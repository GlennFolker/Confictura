package confictura;

import arc.*;
import arc.graphics.*;
import confictura.assets.*;
import confictura.content.*;
import confictura.editor.*;
import confictura.graphics.*;
import confictura.world.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;

import static mindustry.Vars.*;

/**
 * The mod's main class. Contains static references to mod class instances.
 * @author GlennFolker
 */
public class ConficturaMod extends Mod{
    public ConficturaMod(){
        Events.on(FileTreeInitEvent.class, e -> Core.app.post(() -> {
            if(!headless) CShaders.load();
            CCacheLayer.load();

            WorldState.configure();
            Editors.configure();
        }));

        Events.on(ClientLoadEvent.class, e -> mods.getScripts().runConsole("""
            importPackage(java.lang);
            importPackage(Packages.rhino);

            iclass("confictura.world.WorldState")
            iclass("confictura.assets.CShaders")

            function iclass(name){
                importClass(new NativeJavaClass(Vars.mods.getScripts().scope, Class.forName(name, true, Vars.mods.mainLoader())));
            }

            function loadCollapse(){
                WorldState.read(JsonIO.json.fromJson(StringMap, java.lang.String, Vars.state.map.tags.get(WorldState.dataKey, "{}")), WorldState.collapseGrid);
                WorldState.init();
            }
            """
        ));
    }

    @Override
    public void loadContent(){
        CBlocks.load();
        CTerrains.load();
        CPlanets.load();
        CUnitTypes.load();
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
