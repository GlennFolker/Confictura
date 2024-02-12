package confictura.graphics;

import arc.assets.*;
import gltfrenzy.loader.NodeLoader.*;
import gltfrenzy.loader.Scenes3DLoader.*;
import gltfrenzy.model.*;

import static arc.Core.*;

/**
 * Defines the {@link Scenes3D 3D model}s this mod offers.
 * @author GlennFolker
 */
public final class CModels{
    public static Scenes3D portal;

    public static AssetDescriptor<Node> portalStructure;

    private CModels(){
        throw new AssertionError();
    }

    /** Loads the 3D models. Client-side and main thread only! */
    public static void load(){
        portalStructure = assets.load("scenes/confictura/portal.glb#structure", Node.class, new NodeParameter(new Scenes3DParameter(portal = new Scenes3D())));
    }
}
