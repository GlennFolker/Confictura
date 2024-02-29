package confictura.content;

import confictura.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;

/**
 * Defines the {@link Block blocks} this mod offers.
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
        ludellyte = new Floor("ludellyte");

        sharpSlate = new Floor("sharp-slate");

        infSharpSlate = new Floor("infused-sharp-slate");

        archSharpSlate = new Floor("archaic-sharp-slate");

        sharpSlateWall = new StaticWall("sharp-slate-wall"){{
            sharpSlate.asFloor().wall = this;
        }};

        infSharpSlateWall = new StaticWall("infused-sharp-slate-wall"){{
            infSharpSlate.asFloor().wall = this;
        }};

        archSharpSlateWall = new StaticWall("archaic-sharp-slate-wall"){{
            archSharpSlate.asFloor().wall = this;
        }};

        erodedSlate = new Floor("eroded-slate");

        infErodedSlate = new Floor("infused-eroded-slate");

        archErodedSlate = new Floor("archaic-eroded-slate");

        erodedSlateWall = new StaticWall("eroded-slate-wall"){{
            sharpSlate.asFloor().wall = this;
        }};

        infErodedSlateWall = new StaticWall("infused-eroded-slate-wall"){{
            infSharpSlate.asFloor().wall = this;
        }};

        archErodedSlateWall = new StaticWall("archaic-eroded-slate-wall"){{
            archSharpSlate.asFloor().wall = this;
        }};
    }
}
