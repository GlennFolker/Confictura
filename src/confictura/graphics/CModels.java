package confictura.graphics;

import arc.graphics.*;
import gltfrenzy.loader.*;
import gltfrenzy.loader.NodeLoader.*;
import gltfrenzy.loader.Scenes3DLoader.*;
import gltfrenzy.model.*;

import static arc.Core.*;
import static mindustry.Vars.*;

/**
 * Defines the {@linkplain Scenes3D 3D model}s this mod offers.
 * @author GlennFolker
 */
public final class CModels{
    public static Scenes3D portal;

    public static Node portalBase, portalCage;
    public static Mesh slateSpire;

    private CModels(){
        throw new AssertionError();
    }

    /** Loads the 3D models. Client-side and main thread only! */
    public static void load(){
        assets.setLoader(Scenes3D.class, ".gltf", new Scenes3DLoader(tree, new GltfReader()));
        assets.setLoader(Scenes3D.class, ".glb", new Scenes3DLoader(tree, new GlbReader()));

        assets.setLoader(MeshSet.class, new MeshSetLoader(tree));
        assets.setLoader(Node.class, new NodeLoader(tree));

        assets
            .load("scenes/confictura/portal.gltf#Base", Node.class, new NodeParameter(new Scenes3DParameter(portal = new Scenes3D())))
            .loaded = node -> portalBase = node;
        assets
            .load("scenes/confictura/portal.gltf#Cage", Node.class)
            .loaded = node -> portalCage = node;

        assets
            .load("scenes/confictura/spires.gltf#Spire", MeshSet.class)
            .loaded = set -> slateSpire = set.containers.first().mesh;
    }
}
