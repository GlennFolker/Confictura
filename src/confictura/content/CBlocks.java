package confictura.content;

import confictura.*;
import confictura.world.blocks.environment.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;

import static confictura.graphics.CPal.*;

/**
 * Defines the {@linkplain Block blocks} this mod offers.
 * @author GlennFolker
 */
public final class CBlocks{
    public static Block
        ludellyte,
        sharpSlate, infSharpSlate, archSharpSlate, sharpSlateWall, infSharpSlateWall, archSharpSlateWall,
        erodedSlate, infErodedSlate, archErodedSlate, erodedSlateWall, infErodedSlateWall, archErodedSlateWall,
        eneraphyteCrystal, eneraphyteVent, erodedEneraphyteVent;

    private CBlocks(){
        throw new AssertionError();
    }

    /** Instantiates all contents. Called in the main thread in {@link ConficturaMod#loadContent()}. */
    public static void load(){
        ludellyte = new CFloor("ludellyte");

        sharpSlate = new CFloor("sharp-slate");

        infSharpSlate = new CFloor("infused-sharp-slate"){{
            emitLight = true;
            lightColor.set(monolithDark).a(0.12f);
            lightRadius = 48f;
        }};

        archSharpSlate = new CFloor("archaic-sharp-slate"){{
            emitLight = true;
            lightColor.set(monolithMid).a(0.18f);
            lightRadius = 48f;
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

        erodedSlate = new CFloor("eroded-slate");

        infErodedSlate = new CFloor("infused-eroded-slate"){{
            emitLight = true;
            lightColor.set(monolithDarker).a(0.12f);
            lightRadius = 48f;
        }};

        archErodedSlate = new CFloor("archaic-eroded-slate"){{
            emitLight = true;
            lightColor.set(monolithDark).a(0.18f);
            lightRadius = 48f;
        }};

        erodedSlateWall = new StaticWall("eroded-slate-wall"){{
            sharpSlate.asFloor().wall = this;
        }};

        infErodedSlateWall = new StaticWall("infused-eroded-slate-wall"){{
            infSharpSlate.asFloor().wall = this;
            emitLight = true;
            lightColor.set(monolithDark).a(0.12f);
            lightRadius = 48f;
        }};

        archErodedSlateWall = new StaticWall("archaic-eroded-slate-wall"){{
            archSharpSlate.asFloor().wall = this;
            emitLight = true;
            lightColor.set(monolithMid).a(0.18f);
            lightRadius = 48f;
        }};
    }
}
