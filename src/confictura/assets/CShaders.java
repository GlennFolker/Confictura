package confictura.assets;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.struct.*;
import confictura.graphics.GLBuffer.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;
import mindustry.mod.*;

import static mindustry.Vars.*;

/**
 * Defines all the shaders this mod provides, as well as hooks some rendering codes.
 * @author GlennFolker
 */
public final class CShaders{
    /** Buffers everything; to be used with several shaders that need to back-read rendered pixels. */
    public static FrameBuffer screenBuffer;
    /** To avoid writing to the currently read {@link #screenBuffer}. */
    public static FrameBuffer subBuffer;

    public static SlashShaderContainer slash;

    private CShaders(){
        throw new AssertionError();
    }

    /** Initializes this class' shaders and hooks rendering codes. Should be called in {@link Mod#loadContent()}. */
    public static void load(){
        screenBuffer = new FrameBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());
        subBuffer = new FrameBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());

        Events.run(Trigger.draw, () -> {
            subBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
            screenBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
            screenBuffer.begin(Color.clear);
        });

        Events.run(Trigger.drawOver, () -> Draw.draw(Layer.endPixeled - 0.01f, () -> {
            screenBuffer.end();

            Blending.disabled.apply();
            screenBuffer.blit(Shaders.screenspace);
        }));

        slash = new SlashShaderContainer();
    }

    public static class SlashShaderContainer{
        public int noiseOctaves = 4;
        public float noiseScale = 25.0f;
        public float noiseLacunarity = 0.6f;
        public float noisePersistence = 0.5f;
        public float noiseMagnitude = 8f;
        public float blend = 1f;

        protected SlashShader shader;
        protected final FloatSeq uniform;
        protected final FloatSeq attribute;
        protected final ArrayBuffer buffer;

        public SlashShaderContainer(){
            int uniformCount = 128, attributeCount = uniformCount * 4;

            uniform = new FloatSeq(uniformCount);
            attribute = new FloatSeq(attributeCount);
            buffer = new ArrayBuffer(false, attributeCount);
            shader = new SlashShader(this);
        }

        public SlashShader getShader(){
            return shader;
        }

        public void begin(){
            uniform.clear();
            attribute.clear();
        }

        public void end(){
            if(shader.dataCount < uniform.items.length){
                shader.dispose();
                shader = new SlashShader(this);
            }

            buffer.set(attribute.items, 0, attribute.size);
        }

        public SlashShaderContainer uniform(float angle, float length){
            uniform.add(angle, length);
            return this;
        }

        public SlashShaderContainer attribute(float center, float intensity, int index){
            attribute.add(center, intensity, index);
            return this;
        }
    }

    public static class SlashShader extends Shader{
        protected final int dataCount;
        protected final SlashShaderContainer container;
        protected final VertexAttribute attribute;
        protected final int location;

        public SlashShader(SlashShaderContainer container){
            super(
                getFile("slash.vert").readString(),
                "#define DATA_COUNT " + container.uniform.items.length + "\n" + getFile("slash.frag").readString()
            );
            this.container = container;
            dataCount = container.uniform.items.length;

            attribute = new VertexAttribute(3, Gl.floatV, false, "a_slashInput");
            location = getAttributeLocation(attribute.alias);
        }

        @Override
        public void apply(){
            // Bind a non-SpriteBatch vertex attribute to a custom buffer.
            container.buffer.bind();
            Gl.enableVertexAttribArray(location);
            Gl.vertexAttribPointer(location, attribute.components, attribute.type, attribute.normalized, attribute.size, 0);
            container.buffer.unbind();

            // Set relevant uniform.
            setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2f, Core.camera.position.y - Core.camera.height / 2f);
            setUniformf("u_resolution", Core.camera.width, Core.camera.height);
            setUniformf("u_viewport", Core.graphics.getWidth(), Core.graphics.getHeight());
            setUniform2fv("u_slashVerts", container.uniform.items, 0, container.uniform.size);
            setUniformi("u_slashVertsLen", container.uniform.size);

            setUniformi("u_noiseOct", container.noiseOctaves);
            setUniformf("u_noiseScl", container.noiseScale);
            setUniformf("u_noiseLac", container.noiseLacunarity);
            setUniformf("u_noisePer", container.noisePersistence);
            setUniformf("u_noiseMag", container.noiseMagnitude);
            setUniformf("u_blend", container.blend);

            // First, bind the screen texture to active unit 1...
            screenBuffer.getTexture().bind(1);
            setUniformi("u_screenTexture", 1);

            // ... then, just set the active unit to 0, and let the sprite batch bind its texture by itself.
            Gl.activeTexture(Gl.texture0);
        }

        @Override
        public void disableVertexAttribute(String name){
            super.disableVertexAttribute(name);
            if(name.equals("a_position")) super.disableVertexAttribute("a_slashInput");
        }

        @Override
        public void disableVertexAttribute(int location){
            super.disableVertexAttribute(location);
            if(location == getAttributeLocation("a_position")) super.disableVertexAttribute(this.location);
        }
    }

    /**
     * @param name The shader file name, along with its file extension.
     * @return     The shader file that is listed in the mod file tree.
     */
    public static Fi getFile(String name){
        return tree.get("shaders/" + name);
    }
}
