package confictura.graphics;

import arc.graphics.*;
import arc.util.*;

import java.nio.*;

/**
 * A {@link Mesh}-independent OpenGL buffer wrapper class. Must be {@link #dispose()}-ed when no longer used.
 * @author GlennFolker
 */
public abstract class GLBuffer implements Disposable{
    protected int handle;
    protected int usage;

    protected int size;
    protected ByteBuffer buffer;

    public GLBuffer(boolean isStatic, int initialSize){
        handle = Gl.genBuffer();
        usage = isStatic ? Gl.staticDraw : Gl.dynamicDraw;

        size = initialSize;
        buffer = Buffers.newUnsafeByteBuffer(initialSize);
    }

    public void resize(int size){
        if(this.size >= size) return;
        this.size = size;

        Buffers.disposeUnsafeByteBuffer(buffer);
        buffer = Buffers.newUnsafeByteBuffer(size);
    }

    @Override
    public void dispose(){
        Gl.deleteBuffer(handle);
        handle = 0;

        Buffers.disposeUnsafeByteBuffer(buffer);
    }

    @Override
    public boolean isDisposed(){
        return handle == 0;
    }

    /**
     * {@link GLBuffer} specialized for float array buffers.
     * @author GlennFolker
     */
    public static class ArrayBuffer extends GLBuffer{
        protected FloatBuffer buffer;

        public ArrayBuffer(boolean isStatic, int length){
            super(isStatic, length * 4);
            buffer = super.buffer.asFloatBuffer();

            Gl.bufferData(Gl.arrayBuffer, super.buffer.capacity(), buffer, usage);
        }

        public void bind(){
            Gl.bindBuffer(Gl.arrayBuffer, handle);
        }

        public void set(float[] src, int offset, int length, int bufferOffset){
            resize(length * 4);

            super.buffer.clear();
            Buffers.copy(src, super.buffer, length, offset);
            buffer.position(0).limit(length);

            Gl.bindBuffer(Gl.arrayBuffer, handle);
            Gl.bufferSubData(Gl.arrayBuffer, bufferOffset * 4, length * 4, buffer);
        }

        @Override
        public void dispose(){
            Gl.bindBuffer(Gl.arrayBuffer, 0);
            super.dispose();
        }
    }
}
