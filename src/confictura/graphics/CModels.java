package confictura.graphics;

import gltfrenzy.loader.NodeLoader.*;
import gltfrenzy.loader.Scenes3DLoader.*;
import gltfrenzy.model.*;

import static arc.Core.*;

/**
 * Defines the {@linkplain Scenes3D 3D model}s this mod offers.
 * @author GlennFolker
 */
public final class CModels{
    public static Scenes3D portal;

    public static Node portalBase, portalCage;

    private CModels(){
        throw new AssertionError();
    }

    /** Loads the 3D models. Client-side and main thread only! */
    public static void load(){
        assets
            .load("scenes/confictura/portal.gltf#Base", Node.class, new NodeParameter(new Scenes3DParameter(portal = new Scenes3D())))
            .loaded = node -> portalBase = node;
        assets
            .load("scenes/confictura/portal.gltf#Cage", Node.class)
            .loaded = node -> portalCage = node;
    }
}
