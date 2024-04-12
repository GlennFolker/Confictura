package confictura.graphics.g2d;

import mindustry.graphics.*;
import mindustry.graphics.MultiPacker.*;

/**
 * An extension for {@link MultiPacker} that allows deleting the pixmaps. Only ever used in icon generation where
 * temporary/pipeline-control sprites need not be in the final atlas to optimize used space.
 * @author GlFolker
 */
public interface FreeablePacker{
    void delete(String name);

    void delete(PageType type, String name);
}
