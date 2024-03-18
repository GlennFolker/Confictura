package confictura.graphics.g2d;

import arc.graphics.g2d.*;

/**
 * An extension for {@link TextureAtlas} that allows deleting the texture regions. Only ever used in icon generation where
 * temporary/pipeline-control sprites need not be in the final atlas to optimize used space.
 * @author GlennFolker
 */
public interface FreeableAtlas{
    void delete(String name);

    void delete(TextureRegion region);
}
