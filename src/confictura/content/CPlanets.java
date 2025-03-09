package confictura.content;

import arc.graphics.*;
import arc.math.geom.*;
import arc.util.noise.*;
import confictura.*;
import confictura.graphics.*;
import confictura.world.celestial.*;
import confictura.world.celestial.mesh.*;
import confictura.world.celestial.mesh.DualHexMesh.*;
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

        new Planet("test", blackHole, 1f){{
            generator = new SerpuloPlanetGenerator();
            meshLoader = () -> new DualHexMesh(this, 6, CShaders.celestial, new DualHexMesher(){
                @Override
                public void topology(Vec3 position, Vec2 out){
                    out.set(
                        0.5f + Simplex.noise3d(0, 7, 0.5f, 1f/3f, position.x + 3144.13491f, position.y + 3762.134719f, position.z + 9124.1498194f),
                        1.7f - (Simplex.noise3d(1, 7, 0.5f, 1f/2f, position.x + 3144.13491f, position.y + 3762.134719f, position.z + 9124.1498194f) + 1f) / 2f
                    );
                }

                @Override
                public void color(Vec3 position, Color outHigh, Color outLow){
                    outHigh.set(0f, 0.6f, 0.6f, 1f);
                    outLow.set(0.6f, 0f, 0.6f, 1f);
                }
            });
            hasAtmosphere = false;
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
