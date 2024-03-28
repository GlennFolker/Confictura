package confictura.graphics;

import arc.files.*;
import arc.graphics.gl.*;
import arc.graphics.gl.GLVersion.*;
import confictura.graphics.shaders.*;
import mindustry.*;

import static arc.Core.*;
import static mindustry.Vars.*;

/**
 * Defines the {@linkplain Shader shaders}s this mod offers.
 * @author GlennFolker
 */
public final class CShaders{
    public static DepthShader depth;
    public static DepthAtmosphereShader depthAtmosphere;
    public static PortalForcefieldShader portalForcefield;
    public static EmissiveBatchShader emissiveBatch;
    public static CelestialShader celestial;
    public static ModelPropShader modelProp;

    private CShaders(){
        throw new AssertionError();
    }

    /** Loads the shaders. Client-side and main thread only! */
    public static void load(){
        String prevVert = Shader.prependVertexCode, prevFrag = Shader.prependFragmentCode;
        Shader.prependVertexCode = Shader.prependFragmentCode = "";

        if(graphics.getGLVersion().type == GlType.OpenGL){
            Shader.prependFragmentCode = "#define HAS_GL_FRAGDEPTH\n";
        }

        depth = new DepthShader();
        depthAtmosphere = new DepthAtmosphereShader();
        portalForcefield = new PortalForcefieldShader();
        emissiveBatch = new EmissiveBatchShader();
        celestial = new CelestialShader();
        modelProp = new ModelPropShader();

        Shader.prependVertexCode = prevVert;
        Shader.prependFragmentCode = prevFrag;
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
