package confictura.graphics;

import arc.*;
import arc.Graphics.Cursor.*;
import arc.graphics.*;
import arc.graphics.gl.*;

import java.lang.reflect.*;

import static arc.Core.*;

public class SizedGraphics extends Graphics{
    private static final Method setCursor, setSystemCursor;

    protected boolean overriding;
    protected int overrideWidth, overrideHeight;

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

    public void override(int dimension, Runnable run){
        override(dimension, dimension, run);
    }

    public void override(int width, int height, Runnable run){
        overriding = true;
        overrideWidth = width;
        overrideHeight = height;

        var prev = graphics;
        graphics = this;

        try{
            run.run();
        }finally{
            overriding = false;
            graphics = prev;
        }
    }

    @Override
    public int getWidth(){
        return overriding ? overrideWidth : graphics.getWidth();
    }

    @Override
    public int getHeight(){
        return overriding ? overrideHeight : graphics.getHeight();
    }

    @Override
    public GL20 getGL20(){
        return graphics.getGL20();
    }

    @Override
    public void setGL20(GL20 gl20){
        graphics.setGL20(gl20);
    }

    @Override
    public GL30 getGL30(){
        return graphics.getGL30();
    }

    @Override
    public void setGL30(GL30 gl30){
        graphics.setGL30(gl30);
    }

    @Override
    public int getBackBufferWidth(){
        return graphics.getBackBufferWidth();
    }

    @Override
    public int getBackBufferHeight(){
        return graphics.getBackBufferHeight();
    }

    @Override
    public long getFrameId(){
        return graphics.getFrameId();
    }

    @Override
    public float getDeltaTime(){
        return graphics.getDeltaTime();
    }

    @Override
    public int getFramesPerSecond(){
        return graphics.getFramesPerSecond();
    }

    @Override
    public GLVersion getGLVersion(){
        return graphics.getGLVersion();
    }

    @Override
    public float getPpiX(){
        return graphics.getPpiX();
    }

    @Override
    public float getPpiY(){
        return graphics.getPpiY();
    }

    @Override
    public float getPpcX(){
        return graphics.getPpcX();
    }

    @Override
    public float getPpcY(){
        return graphics.getPpcY();
    }

    @Override
    public float getDensity(){
        return graphics.getDensity();
    }

    @Override
    public boolean setWindowedMode(int width, int height){
        return graphics.setWindowedMode(width, height);
    }

    @Override
    public void setTitle(String title){
        graphics.setTitle(title);
    }

    @Override
    public void setBorderless(boolean undecorated){
        graphics.setBorderless(undecorated);
    }

    @Override
    public void setResizable(boolean resizable){
        graphics.setResizable(resizable);
    }

    @Override
    public void setVSync(boolean vsync){
        graphics.setVSync(vsync);
    }

    @Override
    public BufferFormat getBufferFormat(){
        return graphics.getBufferFormat();
    }

    @Override
    public boolean supportsExtension(String extension){
        return graphics.supportsExtension(extension);
    }

    @Override
    public boolean isContinuousRendering(){
        return graphics.isContinuousRendering();
    }

    @Override
    public void setContinuousRendering(boolean isContinuous){
        graphics.setContinuousRendering(isContinuous);
    }

    @Override
    public void requestRendering(){
        graphics.requestRendering();
    }

    @Override
    public boolean isFullscreen(){
        return graphics.isFullscreen();
    }

    @Override
    public Cursor newCursor(Pixmap pixmap, int xHotspot, int yHotspot){
        return graphics.newCursor(pixmap, xHotspot, yHotspot);
    }

    @Override
    protected void setCursor(Cursor cursor){
        try{
            setCursor.invoke(graphics, cursor);
        }catch(InvocationTargetException | IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void setSystemCursor(SystemCursor systemCursor){
        try{
            setSystemCursor.invoke(graphics, systemCursor);
        }catch(InvocationTargetException | IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }
}
