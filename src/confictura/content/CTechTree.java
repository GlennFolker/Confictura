package confictura.content;

import static confictura.content.CBlocks.*;
import static confictura.content.CPlanets.*;
import static mindustry.content.TechTree.*;

/**
 * Sets up content {@link TechNode tech tree nodes}. Loaded after every other content is instantiated.
 * @author GlennFolker
 */
public final class CTechTree{
    private CTechTree(){}

    public static void load(){
        portal.techTree = satelliteSerpulo.techTree = satelliteErekir.techTree = nodeRoot("monolith", satelliteIntercom, true, () -> {

        });
    }
}
