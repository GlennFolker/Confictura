package confictura.graphics.shaders;

import confictura.graphics.gl.*;

import static confictura.graphics.CShaders.*;

public class BlackHoleStencilShader extends Gl30Shader{
    public CFrameBuffer src, ref;

    public BlackHoleStencilShader(){
        super(file("black-hole-stencil.vert"), file("black-hole-stencil.frag"));
    }

    @Override
    public void apply(){
        src.getTexture().bind(2);
        src.getDepthTexture().bind(1);
        ref.getDepthTexture().bind(0);

        setUniformi("u_src", 2);
        setUniformi("u_srcDepth", 1);
        setUniformi("u_ref", 0);
    }
}
