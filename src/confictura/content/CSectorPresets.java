package confictura.content;

import confictura.world.*;
import mindustry.type.*;

/**
 * Defines the {@link SectorPreset maps} this mod offers.
 * @author GlennFolker
 */
public final class CSectorPresets{
    private CSectorPresets(){
        throw new AssertionError();
    }

    public static void load(){
        new ScriptedSector("test", CPlanets.portal, 0);
    }
}
