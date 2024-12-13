package confictura.content;

import arc.graphics.*;
import confictura.*;
import confictura.world.celestial.*;
import mindustry.content.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.maps.planet.*;
import mindustry.type.*;

/**
 * Defines the {@linkplain Planet planets} and other celestial objects this mod offers.
 * @author GlFolker
 */
public final class CPlanets{
    public static Planet

    portal,
    satelliteSerpulo, satelliteErekir,

    blackHole;

    private CPlanets(){
        throw new AssertionError();
    }

    /** Instantiates all contents. Called in the main thread in {@link ConficturaMod#loadContent()}. */
    public static void load(){
        portal = new Portal("portal", Planets.sun, 0.6f);

        satelliteSerpulo = new Satellite("satellite-serpulo", Planets.serpulo, 0.525f);

        satelliteErekir = new Satellite("satellite-erekir", Planets.erekir, 0.525f);

        blackHole = new BlackHole("black-hole", 12f){{
            camRadius = -4f;
            orbitSpacing = 16f;
        }};

        for(int i = 0; i < 3; i++){
            new AtmospherePlanet("test" + i, blackHole, 1f){{
                generator = new SerpuloPlanetGenerator();
                meshLoader = () -> new AtmosphereHexMesh(generator, 6);
                cloudMeshLoader = () -> new MultiMesh(
                    new HexSkyMesh(this, 11, 0.15f, 0.13f, 5, new Color().set(Pal.spore).mul(0.9f).a(0.75f), 2, 0.45f, 0.9f, 0.38f),
                    new HexSkyMesh(this, 1, 0.6f, 0.16f, 5, Color.white.cpy().lerp(Pal.spore, 0.55f).a(0.75f), 2, 0.45f, 1f, 0.41f)
                );
                atmosphereColor = Color.valueOf("3c1b8f");
                atmosphereRadIn = 0.02f;
                atmosphereRadOut = 0.3f;
            }};
        }
    }
}
