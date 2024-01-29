package confictura.graphics;

import arc.files.*;
import arc.graphics.gl.*;
import confictura.graphics.shaders.*;
import mindustry.*;

import static mindustry.Vars.*;

/**
 * Defines the {@link Shader shaders}s this mod offers.
 * @author GlennFolker
 */
public final class CShaders{
    public static DepthShader depth;
    public static PortalForcefieldShader portalForcefield;

    private CShaders(){
        throw new AssertionError();
    }

    /** Loads the shaders. Client-side and main thread only! */
    public static void load(){
        depth = new DepthShader();
        portalForcefield = new PortalForcefieldShader();
    }

    /**
     * Resolves shader files from this mod via {@link Vars#tree}.
     * @param name The shader file name, e.g. {@code my-shader.frag}.
     * @return     The shader file, located inside {@code shaders/confictura/}.
     */
    public static Fi file(String name){
        return tree.get("shaders/confictura/" + name);
    }
}
