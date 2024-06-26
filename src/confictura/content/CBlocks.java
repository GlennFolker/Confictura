package confictura.content;

import arc.graphics.*;
import confictura.*;
import confictura.graphics.*;
import confictura.world.blocks.core.*;
import confictura.world.blocks.environment.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;

import static confictura.graphics.CPal.*;

/**
 * Defines the {@linkplain Block blocks} this mod offers.
 * @author GlFolker
 */
public final class CBlocks{
    public static Block

    ludellyte,
    erodedSlate, infErodedSlate, archErodedSlate, erodedEneraphyteVent, erodedSlateWall, infErodedSlateWall, archErodedSlateWall,
    sharpSlate, infSharpSlate, archSharpSlate, eneraphyteVent, sharpSlateWall, infSharpSlateWall, archSharpSlateWall,
    erodedSpire, sharpSpire,
    eneraphyteCrystal,

    satelliteTiling,

    satelliteIntercom;

    private CBlocks(){
        throw new AssertionError();
    }

    /** Instantiates all contents. Called in the main thread in {@link ConficturaMod#loadContent()}. */
    public static void load(){
        ludellyte = new Floor("ludellyte", 3);

        erodedSlate = new EdgeFloor("eroded-slate");

        infErodedSlate = new EdgeFloor("infused-eroded-slate"){{
            emitLight = true;
            lightColor.set(monolithDarker).a(0.12f);
            lightRadius = 48f;
        }};

        archErodedSlate = new EdgeFloor("archaic-eroded-slate"){{
            emitLight = true;
            lightColor.set(monolithDark).a(0.18f);
            lightRadius = 48f;
        }};

        erodedEneraphyteVent = new SizedVent("eroded-eneraphyte-vent", 1, 0){{
            parent = blendGroup = infErodedSlate;
            effect = CFx.erodedEneraphyteSteam;
            effectSpacing = 30f;
        }};

        erodedSlateWall = new StaticWall("eroded-slate-wall"){{
            erodedSlate.asFloor().wall = this;
        }};

        infErodedSlateWall = new StaticWall("infused-eroded-slate-wall"){{
            infErodedSlate.asFloor().wall = this;
            emitLight = true;
            lightColor.set(monolithDark).a(0.12f);
            lightRadius = 48f;
        }};

        archErodedSlateWall = new StaticWall("archaic-eroded-slate-wall"){{
            archErodedSlate.asFloor().wall = this;
            emitLight = true;
            lightColor.set(monolithMid).a(0.18f);
            lightRadius = 48f;
        }};

        sharpSlate = new EdgeFloor("sharp-slate");

        infSharpSlate = new EdgeFloor("infused-sharp-slate"){{
            emitLight = true;
            lightColor.set(monolithDark).a(0.12f);
            lightRadius = 48f;
        }};

        archSharpSlate = new EdgeFloor("archaic-sharp-slate"){{
            emitLight = true;
            lightColor.set(monolithMid).a(0.18f);
            lightRadius = 48f;
        }};

        eneraphyteVent = new SizedVent("eneraphyte-vent", 1, 0){{
            parent = blendGroup = infSharpSlate;
            effect = CFx.eneraphyteSteam;
            effectSpacing = 30f;
        }};

        sharpSlateWall = new StaticWall("sharp-slate-wall"){{
            sharpSlate.asFloor().wall = this;
        }};

        infSharpSlateWall = new StaticWall("infused-sharp-slate-wall"){{
            infSharpSlate.asFloor().wall = this;
            emitLight = true;
            lightColor.set(monolithMid).a(0.12f);
            lightRadius = 48f;
        }};

        archSharpSlateWall = new StaticWall("archaic-sharp-slate-wall"){{
            archSharpSlate.asFloor().wall = this;
            emitLight = true;
            lightColor.set(monolithLight).a(0.18f);
            lightRadius = 48f;
        }};

        erodedSpire = new ModelProp("eroded-spire", erodedSlateWall, new Color(0x454858bf), CModels.spireSmall1, CModels.spireSmall2);
        sharpSpire = new ModelProp("sharp-spire", sharpSlateWall, new Color(0x6e7080bf), CModels.spireSmall1, CModels.spireSmall2);

        satelliteTiling = new Floor("satellite-tiling", 0);

        satelliteIntercom = new SatelliteEntry("satellite-intercom"){{
            unitType = CUnitTypes.parrier;
            size = 4;
            customShadow = true;
        }};
    }
}
