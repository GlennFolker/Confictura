package confictura.graphics.shaders;

import arc.graphics.g3d.*;
import arc.graphics.gl.*;
import arc.math.geom.*;

import static confictura.graphics.CShaders.*;

public class ModelPropShader extends Shader{
    public Camera3D camera;
    public Vec3 lightDir = new Vec3();

    public ModelPropShader(){
        super(file("model-prop.vert"), file("model-prop.frag"));
    }

    @Override
    public void apply(){
        setUniformMatrix4("u_proj", camera.combined.val);
        setUniformf("u_lightDir", lightDir);
    }
}
