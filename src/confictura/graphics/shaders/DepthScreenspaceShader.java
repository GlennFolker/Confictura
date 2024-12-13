package confictura.graphics.shaders;

import confictura.graphics.gl.*;

import static confictura.graphics.CShaders.*;

public class DepthScreenspaceShader extends Gl30Shader{
    public CFrameBuffer buffer;

    public DepthScreenspaceShader(){
        super(file("depth-screenspace.vert"), file("depth-screenspace.frag"));
    }

    @Override
    public void apply(){
        buffer.getTexture().bind(1);
        buffer.getDepthTexture().bind(0);

        setUniformi("u_color", 1);
        setUniformi("u_depth", 0);
    }
}
