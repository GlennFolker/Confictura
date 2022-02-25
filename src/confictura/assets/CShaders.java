package confictura.assets;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
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

    public static SlashShader slash;

    private CShaders(){
        throw new AssertionError();
    }

    /** Initializes this class' shaders and hooks rendering codes. Should be called in {@link Mod#loadContent()}. */
    public static void load(){
        screenBuffer = new FrameBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());
        Events.run(Trigger.draw, () -> {
            screenBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
            screenBuffer.begin(Color.clear);
        });

        Events.run(Trigger.drawOver, () -> Draw.draw(Layer.endPixeled - 0.01f, () -> {
            screenBuffer.end();

            Blending.disabled.apply();
            screenBuffer.blit(Shaders.screenspace);
        }));

        slash = new SlashShader();
    }

    public static class SlashShader extends Shader{
        public float noiseScale;

        public SlashShader(){
            super(getFile("batch.vert"), getFile("slash.frag"));
        }

        @Override
        public void apply(){
            setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2f, Core.camera.position.y - Core.camera.height / 2f);
            setUniformf("u_resolution", Core.camera.width, Core.camera.height);
            setUniformf("u_viewport", Core.graphics.getWidth(), Core.graphics.getHeight());
            setUniformf("u_scale", noiseScale);

            // First, bind the screen texture to active unit 1...
            screenBuffer.getTexture().bind(1);
            setUniformi("u_screenTexture", 1);

            // ... then, just set the active unit to 0, and let the sprite batch bind its texture by itself.
            Gl.activeTexture(Gl.texture0);
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
