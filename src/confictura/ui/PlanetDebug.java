package confictura.ui;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import confictura.graphics.*;
import mindustry.game.EventType.*;

import static mindustry.Vars.*;

/**
 * Debugging element added to the planet dialog. Currently draws an infinite grid for helpers.
 * @author GlennFolker
 */
public class PlanetDebug{
    public boolean enabled = false;

    public PlanetDebug(){
        Events.run(Trigger.universeDrawEnd, this::draw);
    }

    public void draw(){
        if(!enabled) return;
        Gl.enable(Gl.blend);

        var shader = CShaders.planetDebug;
        shader.camera = renderer.planets.cam;
        Draw.blit(shader);

        Gl.disable(Gl.blend);
    }
}
