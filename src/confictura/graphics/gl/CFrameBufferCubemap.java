package confictura.graphics.gl;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.Cubemap.*;
import arc.graphics.Pixmap.*;
import arc.graphics.Texture.*;
import arc.graphics.gl.*;
import arc.struct.*;
import arc.util.*;

import static arc.Core.*;

/**
 * A cubemap framebuffer that requests depth (and stencil) textures instead of renderbuffers, letting users sample from
 * them. Requires at least GL ES 3.0 to function, since the spec is really dumb and decided to include depth and stencil
 * texture attachments at that version.
 * @author GlFolker
 */
public class CFrameBufferCubemap extends FrameBufferCubemap{
    public static final CubemapSide[] sides = CubemapSide.values();

    protected Format format;
    protected boolean hasDepth, hasStencil;

    public CFrameBufferCubemap(int width, int height, boolean hasDepth){
        this(Format.rgba8888, width, height, hasDepth, false);
    }

    public CFrameBufferCubemap(Format format, int width, int height, boolean hasDepth, boolean hasStencil){
        super(create(format, width, height, hasDepth, hasStencil));
        this.format = format;
        this.hasDepth = hasDepth;
        this.hasStencil = hasStencil;
    }

    public static FrameBufferCubemapBuilder create(Format format, int width, int height, boolean hasDepth, boolean hasStencil){
        if(width < 2) width = 2;
        if(height < 2) height = 2;

        var builder = new FrameBufferCubemapBuilder(width, height);
        builder.addBasicColorTextureAttachment(format);
        if(hasDepth) builder.addDepthTextureAttachment(Gl.depthComponent, Gl.floatV);
        if(hasStencil) builder.addStencilTextureAttachment(Gl.stencilIndex8, Gl.unsignedByte);

        return builder;
    }

    @Override
    protected void build(){
        if(!defaultFramebufferHandleInitialized){
            defaultFramebufferHandleInitialized = true;
            defaultFramebufferHandle = 0; // Java mods don't work on iOS anyway.
        }

        int lastHandle = currentBoundFramebuffer == null ? defaultFramebufferHandle : currentBoundFramebuffer.getFramebufferHandle();

        framebufferHandle = Gl.genFramebuffer();
        Gl.bindFramebuffer(Gl.framebuffer, framebufferHandle);

        // And here we see one of the most horrendous code ever due to Java's stupid tendency of making everything private for no reason.
        int width = Reflect.get(GLFrameBufferBuilder.class, bufferBuilder, "width");
        int height = Reflect.get(GLFrameBufferBuilder.class, bufferBuilder, "height");

        var specs = Reflect.<Seq<FrameBufferTextureAttachmentSpec>>get(GLFrameBufferBuilder.class, bufferBuilder, "textureAttachmentSpecs");
        isMRT = specs.size > 1;

        int colorTextureCounter = 0;
        if(isMRT){
            for(var spec : specs){
                var texture = createTexture(spec);
                textureAttachments.add(texture);

                if(spec.isColorTexture()){
                    attachTexture(Gl.colorAttachment0 + colorTextureCounter, texture);
                    colorTextureCounter++;
                }else if(Reflect.get(FrameBufferTextureAttachmentSpec.class, spec, "isDepth")){
                    attachTexture(Gl.depthAttachment, texture);
                }else if(Reflect.get(FrameBufferTextureAttachmentSpec.class, spec, "isStencil")){
                    attachTexture(Gl.stencilAttachment, texture);
                }
            }
        }else{
            var texture = createTexture(specs.first());
            textureAttachments.add(texture);
            attachTexture(Gl.colorAttachment0, texture);
        }

        if(isMRT){
            var buffer = Buffers.newIntBuffer(colorTextureCounter);
            for(int i = 0; i < colorTextureCounter; i++){
                buffer.put(Gl.colorAttachment0 + i);
            }

            buffer.position(0);
            gl30.glDrawBuffers(colorTextureCounter, buffer);
        }

        for(var texture : textureAttachments) Gl.bindTexture(texture.glTarget, 0);
        int result = Gl.checkFramebufferStatus(Gl.framebuffer);

        Gl.bindFramebuffer(Gl.framebuffer, lastHandle);
        if(result != Gl.framebufferComplete){
            for(var texture : textureAttachments) disposeColorTexture(texture);

            Gl.deleteFramebuffer(framebufferHandle);
            if(result == Gl.framebufferIncompleteAttachment)
                throw new IllegalStateException("Frame buffer couldn't be constructed: incomplete attachment (" + width + "x" + height + ")");
            if(result == Gl.framebufferIncompleteDimensions)
                throw new IllegalStateException("Frame buffer couldn't be constructed: incomplete dimensions");
            if(result == Gl.framebufferIncompleteMissingAttachment)
                throw new IllegalStateException("Frame buffer couldn't be constructed: missing attachment");
            if(result == Gl.framebufferUnsupported)
                throw new IllegalStateException("Frame buffer couldn't be constructed: unsupported combination of formats");
            throw new IllegalStateException("Frame buffer couldn't be constructed: unknown error " + result);
        }
    }

    @Override
    protected Cubemap createTexture(FrameBufferTextureAttachmentSpec spec){
        var result = super.createTexture(spec);
        if(!spec.isColorTexture()) result.setFilter(TextureFilter.nearest);

        return result;
    }

    protected void attachTexture(int attachment, Cubemap texture){
        int glHandle = texture.getTextureObjectHandle();
        var sides = CubemapSide.values();
        for(var side : sides) Gl.framebufferTexture2D(Gl.framebuffer, attachment, side.glEnum, glHandle, 0);
    }

    public @Nullable Cubemap getDepthTexture(){
        return hasDepth ? textureAttachments.get(1) : null;
    }

    public @Nullable Cubemap getStencilTexture(){
        return hasStencil ? textureAttachments.get(hasDepth ? 2 : 1) : null;
    }

    @Override
    protected void bindSide(CubemapSide side){
        Gl.framebufferTexture2D(Gl.framebuffer, Gl.colorAttachment0, side.glEnum, getTexture().getTextureObjectHandle(), 0);
        if(hasDepth) Gl.framebufferTexture2D(Gl.framebuffer, Gl.depthAttachment, side.glEnum, getDepthTexture().getTextureObjectHandle(), 0);
        if(hasStencil) Gl.framebufferTexture2D(Gl.framebuffer, Gl.stencilAttachment, side.glEnum, getStencilTexture().getTextureObjectHandle(), 0);
    }

    public void resize(int width, int height){
        if(width < 2) width = 2;
        if(height < 2) height = 2;

        if(width == getWidth() && height == getHeight()) return;

        TextureFilter min = getTexture().getMinFilter(), mag = getTexture().getMagFilter();
        dispose();

        bufferBuilder = create(format, width, height, hasDepth, hasStencil);
        textureAttachments.clear();
        framebufferHandle = depthbufferHandle = stencilbufferHandle = depthStencilPackedBufferHandle = 0;
        hasDepthStencilPackedBuffer = isMRT = false;

        build();

        // Ignore filters for depth and stencil textures, as changing them in the first place is always a wrong choice.
        getTexture().setFilter(min, mag);
    }

    public void eachSide(Cons<CubemapSide> cons){
        for(var side : sides){
            bindSide(side);
            cons.get(side);
        }
    }

    @Override
    public final boolean nextSide(){
        throw new UnsupportedOperationException("Use `iterateSide()` instead!");
    }

    @Override
    public final CubemapSide getSide(){
        throw new UnsupportedOperationException("Use `iterateSide()` instead!");
    }
}
