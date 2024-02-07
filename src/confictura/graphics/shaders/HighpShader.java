package confictura.graphics.shaders;

import arc.files.*;
import arc.graphics.gl.*;

import static confictura.graphics.CShaders.*;

/**
 * Opts-in to supporting IEEE 754 floating-point precision in desktop GL.
 * @author GlennFolker
 */
public class HighpShader extends Shader{
    public HighpShader(String vertexShader, String fragmentShader){
        super(vertexShader, fragmentShader);
    }

    public HighpShader(Fi vertexShader, Fi fragmentShader){
        super(vertexShader, fragmentShader);
    }

    @Override
    protected String preprocess(String source, boolean fragment){
        return preprocessHighp(source, fragment);
    }
}
