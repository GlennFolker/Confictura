package confictura.content;

import confictura.*;
import mindustry.type.*;

/**
 * Defines the {@linkplain SectorPreset maps} this mod offers.
 * @author GlFolker
 */
public final class CSectorPresets{
    private CSectorPresets(){
        throw new AssertionError();
    }

    /** Instantiates all contents. Called in the main thread in {@link ConficturaMod#loadContent()}. */
    public static void load(){
        new SectorPreset("test", CPlanets.portal, 0);
    }
}
