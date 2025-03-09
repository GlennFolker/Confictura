package confictura.graphics;

import arc.*;
import arc.Graphics.Cursor.*;
import arc.graphics.*;
import arc.graphics.gl.*;

import java.lang.reflect.*;

/**
 * A {@link Graphics} mock-module used to logically pretend that the window's screen resolution is something else. This
 * is typically done to adjust rendering code to a {@linkplain FrameBuffer framebuffer}'s size that is different from
 * the screen by overriding what {@link Graphics#getWidth()} and {@link Graphics#getHeight()} return.
 * <br>
 * Call {@link #override(int, Runnable)} or {@link #override(int, int, Runnable)}, and the supplied runnable will be
 * immediately run with {@link Core#graphics} having the overridden screen dimensions.
 * @author GlFolker
 */
public class SizedGraphics extends Graphics{
    private static final Method setCursor, setSystemCursor;

    protected int overrideWidth, overrideHeight;
    protected Graphics delegate;

    static{
        try{
            setCursor = Graphics.class.getDeclaredMethod("setCursor", Cursor.class);
            setSystemCursor = Graphics.class.getDeclaredMethod("setSystemCursor", SystemCursor.class);

            setCursor.setAccessible(true);
            setSystemCursor.setAccessible(true);
        }catch(NoSuchMethodException e){
            throw new RuntimeException(e);
        }
    }

    /** @see #override(int, int, Runnable)  */
    public void override(int dimension, Runnable run){
        override(dimension, dimension, run);
    }

    /**
     * Momentarily assigns this instance to {@link Core#graphics}, overriding its screen dimension. The supplied
     * runnable will be run within that context and once it exits {@link Core#graphics} will be restored.
     * @param width  The override width that'll be returned by {@link Graphics#getWidth()}.
     * @param height The override height that'll be returned by {@link Graphics#getHeight()}.
     * @param run    The runnable that will be immediately run within the overridden graphical context.
     */
    public void override(int width, int height, Runnable run){
        overrideWidth = width;
        overrideHeight = height;

        var prev = Core.graphics;
        var prevDelegate = delegate; // Safe-guard against nested `override()` calls.

        Core.graphics = this;
        delegate = prev;

        try{
            run.run();
        }finally{
            Core.graphics = prev;
            delegate = prevDelegate;
        }
    }

    @Override
    public boolean isGL30Available(){
        return delegate.isGL30Available();
    }

    @Override
    public GL20 getGL20(){
        return delegate.getGL20();
    }

    @Override
    public void setGL20(GL20 gl20){
        delegate.setGL20(gl20);
    }

    @Override
    public GL30 getGL30(){
        return delegate.getGL30();
    }

    @Override
    public void setGL30(GL30 gl30){
        delegate.setGL30(gl30);
    }

    @Override
    public void clear(Color color){
        delegate.clear(color);
    }

    @Override
    public void clear(float r, float g, float b, float a){
        delegate.clear(r, g, b, a);
    }

    @Override
    public boolean isPortrait(){
        return delegate.isPortrait();
    }

    @Override
    public int getWidth(){
        return overrideWidth;
    }

    @Override
    public int getHeight(){
        return overrideHeight;
    }

    @Override
    public float getAspect(){
        return (float)overrideWidth / overrideHeight;
    }

    @Override
    public boolean isHidden(){
        return delegate.isHidden();
    }

    @Override
    public int getBackBufferWidth(){
        return delegate.getBackBufferWidth();
    }

    @Override
    public int getBackBufferHeight(){
        return delegate.getBackBufferHeight();
    }

    @Override
    public int[] getSafeInsets(){
        return delegate.getSafeInsets();
    }

    @Override
    public long getFrameId(){
        return delegate.getFrameId();
    }

    @Override
    public float getDeltaTime(){
        return delegate.getDeltaTime();
    }

    @Override
    public int getFramesPerSecond(){
        return delegate.getFramesPerSecond();
    }

    @Override
    public GLVersion getGLVersion(){
        return delegate.getGLVersion();
    }

    @Override
    public float getPpiX(){
        return delegate.getPpiX();
    }

    @Override
    public float getPpiY(){
        return delegate.getPpiY();
    }

    @Override
    public float getPpcX(){
        return delegate.getPpcX();
    }

    @Override
    public float getPpcY(){
        return delegate.getPpcY();
    }

    @Override
    public float getDensity(){
        return delegate.getDensity();
    }

    @Override
    public boolean setFullscreen(){
        return delegate.setFullscreen();
    }

    @Override
    public boolean setWindowedMode(int width, int height){
        return delegate.setWindowedMode(width, height);
    }

    @Override
    public void setTitle(String title){
        delegate.setTitle(title);
    }

    @Override
    public void setBorderless(boolean undecorated){
        delegate.setBorderless(undecorated);
    }

    @Override
    public void setResizable(boolean resizable){
        delegate.setResizable(resizable);
    }

    @Override
    public void setVSync(boolean vsync){
        delegate.setVSync(vsync);
    }

    @Override
    public BufferFormat getBufferFormat(){
        return delegate.getBufferFormat();
    }

    @Override
    public boolean supportsExtension(String extension){
        return delegate.supportsExtension(extension);
    }

    @Override
    public boolean isContinuousRendering(){
        return delegate.isContinuousRendering();
    }

    @Override
    public void setContinuousRendering(boolean isContinuous){
        delegate.setContinuousRendering(isContinuous);
    }

    @Override
    public void requestRendering(){
        delegate.requestRendering();
    }

    @Override
    public boolean isFullscreen(){
        return delegate.isFullscreen();
    }

    @Override
    public Cursor newCursor(String filename){
        return delegate.newCursor(filename);
    }

    @Override
    public Cursor newCursor(String filename, int scale){
        return delegate.newCursor(filename, scale);
    }

    @Override
    public Cursor newCursor(String filename, int scaling, Color outlineColor, int outlineScaling){
        return delegate.newCursor(filename, scaling, outlineColor, outlineScaling);
    }

    @Override
    public Cursor newCursor(Pixmap pixmap, int xHotspot, int yHotspot){
        return delegate.newCursor(pixmap, xHotspot, yHotspot);
    }

    @Override
    public Cursor newCursor(Pixmap pixmap, int scaling, Color outlineColor, int outlineThickness){
        return delegate.newCursor(pixmap, scaling, outlineColor, outlineThickness);
    }

    @Override
    public void restoreCursor(){
        delegate.restoreCursor();
    }

    @Override
    public void cursor(Cursor cursor){
        delegate.cursor(cursor);
    }

    @Override
    protected void setCursor(Cursor cursor){
        try{
            setCursor.invoke(delegate, cursor);
        }catch(InvocationTargetException | IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void setSystemCursor(SystemCursor systemCursor){
        try{
            setSystemCursor.invoke(delegate, systemCursor);
        }catch(InvocationTargetException | IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void dispose(){
        delegate.dispose();
    }

    @Override
    public boolean isDisposed(){
        return delegate.isDisposed();
    }
}
