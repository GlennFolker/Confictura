package confictura.graphics;

import arc.files.*;
import arc.graphics.gl.*;
import confictura.graphics.shaders.*;
import mindustry.*;

import static arc.Core.*;
import static confictura.ConficturaMod.*;
import static mindustry.Vars.*;

/**
 * Defines the {@link Shader shaders}s this mod offers.
 * @author GlennFolker
 */
public final class CShaders{
    public static DepthShader depth;
    public static DepthAtmosphereShader depthAtmosphere;
    public static PortalForcefieldShader portalForcefield;

    private CShaders(){
        throw new AssertionError();
    }

    /** Loads the shaders. Client-side and main thread only! */
    public static void load(){
        depth = new DepthShader();
        depthAtmosphere = new DepthAtmosphereShader();
        portalForcefield = new PortalForcefieldShader();
    }

    /** @return {@code true} if shaders support 32-bit floats according to the IEEE 754 standard. */
    public static boolean highpFloat(){
        return !app.isDesktop() || glsl33;
    }

    public static String preprocessHighp(String source, boolean fragment){
        if(source.contains("#ifdef GL_ES")){
            throw new IllegalArgumentException("Shader contains GL_ES specific code; this should be handled by the preprocessor. Code: \n```\n" + source + "\n```");
        }

        if(source.contains("#version")){
            throw new IllegalArgumentException("Shader contains explicit version requirement; this should be handled by the preprocessor. Code: \n```\n" + source + "\n```");
        }

        if(fragment){
            source =
                "#ifdef GL_ES\n" +
                "precision " + (source.contains("#define HIGHP") && !source.contains("//#define HIGHP") ? "highp" : "mediump") + " float;\n" +
                "precision mediump int;\n" +
                "#else\n" +
                "#define lowp\n" +
                "#define mediump\n" +
                "#define highp\n" +
                "#endif\n" + source;
        }else{
            source =
                "#ifndef GL_ES\n" +
                "#define lowp\n" +
                "#define mediump\n" +
                "#define highp\n" +
                "#endif\n" + source;
        }

        /*if(glsl33){
            source = ("#version 330 core\n" + (fragment ? "out vec4 fragColor;\n" : "") + source)
                .replace("varying", fragment ? "in" : "out")
                .replace("attribute", fragment ? "???" : "in")
                .replace("texture2D(", "texture(")
                .replace("textureCube(", "texture(")
                .replace("gl_FragColor", "fragColor");
        }*/

        return source;
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
