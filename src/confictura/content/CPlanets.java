package confictura.content;

import confictura.*;
import confictura.world.celestial.*;
import mindustry.content.*;
import mindustry.type.*;

/**
 * Defines the {@linkplain Planet planets} and other celestial objects this mod offers.
 * @author GlennFolker
 */
public final class CPlanets{
    public static Planet

    portal,
    satelliteSerpulo, satelliteErekir;

    private CPlanets(){
        throw new AssertionError();
    }

    /** Instantiates all contents. Called in the main thread in {@link ConficturaMod#loadContent()}. */
    public static void load(){
        portal = new Portal("portal", Planets.sun, 0.6f);

        satelliteSerpulo = new Satellite("satellite-serpulo", Planets.serpulo, 0.525f);

        satelliteErekir = new Satellite("satellite-erekir", Planets.erekir, 0.525f);
    }
}
