package confictura.graphics;

import arc.files.*;
import arc.graphics.*;
import gltfrenzy.loader.*;
import gltfrenzy.model.*;

import static arc.Core.*;
import static confictura.util.StructUtils.*;
import static mindustry.Vars.*;

/**
 * Defines the {@linkplain Scenes3D 3D model}s this mod offers.
 * @author GlennFolker
 */
public final class CModels{
    public static Scenes3D portal, satellite, spires;

    public static Node

    portalBase, portalCage,
    satelliteBase, satelliteThruster, satelliteArmInner, satelliteArmOuter;

    public static Mesh

    spireSmall1, spireSmall2;

    private CModels(){
        throw new AssertionError();
    }

    /** Loads the 3D models. Client-side and main thread only! */
    public static void load(){
        assets.setLoader(Scenes3D.class, ".gltf", new Scenes3DLoader(tree, new GltfReader()));
        assets.setLoader(Scenes3D.class, ".glb", new Scenes3DLoader(tree, new GlbReader()));

        assets.setLoader(MeshSet.class, new MeshSetLoader(tree));
        assets.setLoader(Node.class, new NodeLoader(tree));

        Runnable[] loadSync = {
            (portal = new Scenes3D()).load(file("portal"), tree, null),
            (satellite = new Scenes3D()).load(file("satellite"), tree, null),
            (spires = new Scenes3D()).load(file("spires"), tree, null)
        };

        portalBase = portal.node("Base");
        portalCage = portal.node("Cage");

        satelliteBase = satellite.node("Base");
        satelliteThruster = satellite.node("Thruster");
        satelliteArmInner = satellite.node("ArmInner");
        satelliteArmOuter = satellite.node("ArmOuter");

        app.post(() -> {
            each(loadSync, Runnable::run);
            spireSmall1 = spires.mesh("SpireSmall1").containers.first().mesh;
            spireSmall2 = spires.mesh("SpireSmall2").containers.first().mesh;
        });
    }

    public static Fi file(String name){
        return tree.get("scenes/confictura/" + name + ".gltf");
    }
}
