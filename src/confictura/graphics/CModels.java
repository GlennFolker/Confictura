package confictura.graphics;

import arc.graphics.*;
import gltfrenzy.loader.MeshSetLoader.*;
import gltfrenzy.loader.Scenes3DLoader.*;
import gltfrenzy.model.*;

import static arc.Core.*;

/**
 * Defines the {@link Scenes3D 3D model}s this mod offers.
 * @author GlennFolker
 */
public final class CModels{
    public static Scenes3D portal;

    public static MeshSet portalStructure;

    private CModels(){
        throw new AssertionError();
    }

    /** Loads the 3D models. Client-side and main thread only! */
    public static void load(){
        assets.load("scenes/confictura/portal.glb#foundation", MeshSet.class, new MeshSetParameter(new Scenes3DParameter(portal = new Scenes3D())
            //.skip("foundation", VertexAttribute.texCoords.alias)
            //.skip("foundation", VertexAttribute.color.alias)
        )).loaded = mesh -> portalStructure = mesh;
    }
}
