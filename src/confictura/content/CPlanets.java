package confictura.content;

import confictura.*;
import mindustry.content.*;
import mindustry.type.*;

/**
 * Defines the {@link Planet planets} this mod offers.
 * @author GlennFolker
 */
public final class CPlanets{
    public static Planet outpost;

    private CPlanets(){
        throw new AssertionError();
    }

    /** Instantiates all contents. Called in the main thread in {@link ConficturaMod#loadContent()}. */
    public static void load(){
        outpost = new Planet("outpost", Planets.sun, 0.2f, 0){
            {
                camRadius = 0.4f;
                minZoom = 0f;
            }
        };
    }
}
