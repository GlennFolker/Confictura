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
    public static Node satelliteBase, satelliteThruster, satelliteArmInner, satelliteArmOuter;

    public static Mesh spireSmall1, spireSmall2;

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
            .load("scenes/confictura/satellite.gltf#Base", Node.class)
            .loaded = node -> satelliteBase = node;
        assets
            .load("scenes/confictura/satellite.gltf#Thruster", Node.class)
            .loaded = node -> satelliteThruster = node;
        assets
            .load("scenes/confictura/satellite.gltf#ArmInner", Node.class)
            .loaded = node -> satelliteArmInner = node;
        assets
            .load("scenes/confictura/satellite.gltf#ArmOuter", Node.class)
            .loaded = node -> satelliteArmOuter = node;
        assets
            .load("scenes/confictura/spires.gltf#SpireSmall1", MeshSet.class)
            .loaded = mesh -> spireSmall1 = mesh.containers.first().mesh;
        assets
            .load("scenes/confictura/spires.gltf#SpireSmall2", MeshSet.class)
            .loaded = mesh -> spireSmall2 = mesh.containers.first().mesh;
    }
}
