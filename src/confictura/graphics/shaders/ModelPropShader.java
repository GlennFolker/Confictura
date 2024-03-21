package confictura.graphics.shaders;

import arc.graphics.gl.*;

import static confictura.graphics.CShaders.*;

public class ModelPropShader extends Shader{
    public ModelPropShader(){
        super(file("model-prop.vert"), file("model-prop.frag"));
    }
}
