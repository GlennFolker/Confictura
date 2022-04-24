package confictura.content;

import mindustry.mod.*;
import mindustry.world.*;
import confictura.world.blocks.environment.*;

/**
 * Defines all the blocks this mod provides.
 * @author GlennFolker
 */
public final class CBlocks{
    private CBlocks(){
        throw new AssertionError();
    }

    /** Initializes this class' contents. Should be called in {@link Mod#loadContent()}. */
    public static void load(){
        for(int i = 0; i < 3; i++) new CollapseFloor("collapse-" + i);
    }
}
