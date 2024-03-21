package confictura.world.blocks;

import mindustry.world.*;

/**
 * Uses another block's {@link Block#mapColor mapColor}.
 * @author GlennFolker
 */
public interface DelegateMapColor{
    Block substitute();
}
