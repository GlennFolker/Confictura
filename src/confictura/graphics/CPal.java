package confictura.graphics;

import arc.graphics.*;

/**
 * Static read-only palettes that are used throughout the mod.
 * @author GlennFolker
 */
public final class CPal{
    public static final Color

    monolithLighter = new Color(0x9cd4f8ff),
    monolithLight = new Color(0x72a2d7ff),
    monolithMid = new Color(0x5379b7ff),
    monolithDark = new Color(0x354d97ff),
    monolithDarker = new Color(0x253080ff),

    monolithOutline = new Color(0x38383dff);

    private CPal(){
        throw new AssertionError();
    }
}
