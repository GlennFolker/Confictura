package confictura.assets;

import arc.*;
import arc.files.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import confictura.assets.CShaders.CollapseShader.*;
import confictura.graphics.GLBuffer.*;
import confictura.world.*;
import confictura.world.WorldState.*;
import confictura.world.blocks.environment.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
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
    public static CollapseShader collapse;

    private CShaders(){
        throw new AssertionError();
    }

    /** Initializes this class' shaders and hooks rendering codes. Should be called in {@link Mod#loadContent()}. */
    public static void load(){
        screenBuffer = new FrameBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());
        subBuffer = new FrameBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());

        slash = new SlashShaderContainer();
        Events.on(ContentInitEvent.class, e -> Core.app.post(() -> collapse = new CollapseShader()));

        Events.run(Trigger.draw, () -> {
            subBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
            screenBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
            screenBuffer.begin(Color.clear);
        });

        Events.run(Trigger.drawOver, () -> Draw.draw(Layer.endPixeled - 0.01f, () -> {
            screenBuffer.end();

            Blending.disabled.apply();
            Draw.blit(screenBuffer.getTexture(), Shaders.screenspace);
        }));
    }

    public static class SlashShaderContainer{
        public Color glowColor = new Color();
        public float glowThreshold;

        public int noiseOctaves;
        public float noiseScale;
        public float noiseLacunarity;
        public float noisePersistence;
        public float noiseMagnitude;
        public float blend;

        protected SlashShader shader;
        protected final FloatSeq uniform;
        protected final FloatSeq attribute;
        protected final ArrayBuffer buffer;

        public SlashShaderContainer(){
            uniform = new FloatSeq(192);
            attribute = new FloatSeq(192);
            buffer = new ArrayBuffer(false, 192);
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

        public SlashShaderContainer uniform(float angle, float length, float width){
            uniform.add(angle, length, width);
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
        protected final int location, trigger;

        public SlashShader(SlashShaderContainer container){
            super(
                getFile("slash.vert").readString(),
                "#define DATA_COUNT " + container.uniform.items.length + "\n" + getFile("slash.frag").readString()
            );
            this.container = container;
            dataCount = container.uniform.items.length;

            attribute = new VertexAttribute(3, Gl.floatV, false, "a_slashInput");
            location = getAttributeLocation(attribute.alias);
            trigger = getAttributeLocation("a_position");
        }

        @Override
        public void apply(){
            // Bind a non-SpriteBatch vertex attribute to a custom buffer.
            container.buffer.bind();
            Gl.enableVertexAttribArray(location);
            Gl.vertexAttribPointer(location, attribute.components, attribute.type, attribute.normalized, attribute.size, 0);
            container.buffer.unbind();

            // Set relevant uniforms.
            setUniformf("u_glowColor", container.glowColor);
            setUniformf("u_glowThreshold", container.glowThreshold);

            setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2f, Core.camera.position.y - Core.camera.height / 2f);
            setUniformf("u_resolution", Core.camera.width, Core.camera.height);
            setUniformf("u_viewport", Core.graphics.getWidth(), Core.graphics.getHeight());
            setUniform3fv("u_slashVerts", container.uniform.items, 0, container.uniform.size);
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
            if(location == trigger) super.disableVertexAttribute(this.location);
        }
    }

    public static class CollapseShader extends Shader{
        public float fallScale = 3f;

        public final CollapseDraw[] draws = new CollapseDraw[CollapseFloor.max()];
        public int count;

        /*public CollapseShader(CollapseShaderContainer container, int textureWidth, int textureHeight){
            super(
                Shaders.getShaderFi("screenspace.vert").readString(),
                ("#define DATA_COUNT " + WorldState.collapses.count) + '\n' +
                ("#define TEX_WIDTH " + textureWidth) + '\n' +
                ("#define TEX_HEIGHT " + textureHeight) + '\n' +
                getFile("collapse.frag").readString()
            );

            this.container = container;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
            count = draws.length;
        }*/

        public CollapseShader(){
            super(
                Shaders.getShaderFi("screenspace.vert").readString(),
                ("#define DATA_COUNT " + CollapseFloor.max()) + '\n' + getFile("collapse.frag").readString()
            );
        }

        public void reset(){
            Seq<CollapseData> datas = WorldState.collapses;

            int count = 0;
            for(int i = 0; i < draws.length; i++){
                CollapseDraw draw = draws[i];
                if(draw != null) draw.dispose();

                CollapseData data;
                if(i < datas.size && (data = datas.get(i)).valid()){
                    draws[i] = new CollapseDraw(data);
                    count++;
                }else{
                    draws[i] = null;
                }
            }

            this.count = count;
        }

        @Override
        public void dispose(){
            super.dispose();
            for(int i = 0; i < draws.length; i++){
                CollapseDraw draw = draws[i];
                if(draw != null) draw.dispose();

                draws[i] = null;
            }
        }

        @Override
        public void apply(){
            /*int unit = 31;
            for(int i = 0; i < count; i++){
                CollapseDraw draw = draws[i];
                CollapseData data = draw.data;

                setUniformf("u_datas[" + i + "].bound", data.bound.x, data.bound.y, data.bound.width, data.bound.height);
                setUniformf("u_datas[" + i + "].progress", data.timestamp == -1f ? 0f : Math.min((Time.time - data.timestamp) / data.duration, 1f));
                setUniformf("u_datas[" + i + "].resolution", draw.captureWidth, draw.captureHeight);

                if(data.bound.overlaps(
                    Core.camera.position.x - Core.camera.width / 2f,
                    Core.camera.position.y - Core.camera.height / 2f,
                    Core.camera.width,
                    Core.camera.height
                )){
                    for(int x = 0; x < textureWidth; x++){
                        for(int y = 0; y < textureHeight; y++){
                            FrameBuffer buffer = draw.buffers[x][y];
                            if(buffer == null) continue;

                            buffer.getTexture().bind(unit);
                            setUniformi("u_datas[" + i + "].textures[" + (y * textureWidth + x) + "]", unit);

                            unit--;
                        }
                    }
                }
            }*/

            int unit = count;
            for(int i = 0; i < draws.length; i++){
                CollapseDraw draw = draws[i];
                if(draw == null) continue;

                CollapseData data = draw.data;
                if(data.bound.overlaps(
                    Core.camera.position.x - Core.camera.width / 2f,
                    Core.camera.position.y - Core.camera.height / 2f,
                    Core.camera.width,
                    Core.camera.height
                )){
                    setUniformf("u_datas[" + i + "].bound", data.bound.x, data.bound.y, data.bound.width, data.bound.height);
                    setUniformf("u_datas[" + i + "].progress", data.timestamp == -1f ? 0f : Math.min((Time.time - data.timestamp) / data.duration, 1f));

                    draw.buffer.getTexture().bind(unit);
                    setUniformi("u_datas[" + i + "].texture", unit);

                    unit--;
                }
            }

            renderer.effectBuffer.getTexture().bind(0);
            setUniformi("u_texture", 0);
            setUniformf("u_view", Core.camera.position.x, Core.camera.position.y, Core.camera.width, Core.camera.height);
            setUniformf("u_fallScale", fallScale);
        }

        @SuppressWarnings("unchecked")
        public static class CollapseDraw implements Disposable{
            public CollapseData data;
            public final FrameBuffer buffer;
            //public final FrameBuffer[][] buffers;

            //public int captureWidth, captureHeight;
            //protected int width, height;

            private final Cons<Trigger> capturer = new Cons<>(){
                @Override
                public void get(Trigger trigger){
                    capture();
                    Events.remove((Class<Trigger>)trigger.getClass(), this);
                }
            };

            public CollapseDraw(CollapseData data){
                this.data = data;
                buffer = new FrameBuffer(Mathf.round(data.bound.width * 8f, 32), Mathf.round(data.bound.height * 8f, 32));
                //buffers = new FrameBuffer[textureWidth][textureHeight];
            }

            /*public void createBuffers(){
                dispose();

                int sw = Core.graphics.getWidth(), sh = Core.graphics.getHeight();
                width = Math.min(Mathf.ceil(data.bound.width * 4f / sw), textureWidth);
                height = Math.min(Mathf.ceil(data.bound.height * 4f / sh), textureHeight);

                for(int x = 0; x < width; x++){
                    for(int y = 0; y < height; y++){
                        buffers[x][y] = new FrameBuffer(sw, sh);
                    }
                }
            }*/

            public void prepareCapture(){
                Events.on((Class<Trigger>)Trigger.preDraw.getClass(), capturer);
            }

            public void capture(){
                float
                    cx = Core.camera.position.x, cy = Core.camera.position.y,
                    cw = Core.camera.width, ch = Core.camera.height;

                Core.camera.position.set(data.bound.x + data.bound.width / 2f - 4f, data.bound.y + data.bound.height / 2f - 4f);
                Core.camera.width = data.bound.width;
                Core.camera.height = data.bound.height;
                Core.camera.update();

                buffer.begin(Color.clear);

                Draw.draw(Layer.floor, renderer.blocks.floor::drawFloor);
                Draw.draw(Layer.block - 1, renderer.blocks::drawShadows);
                Draw.draw(Layer.block - 0.09f, () -> {
                    renderer.blocks.floor.beginDraw();
                    renderer.blocks.floor.drawLayer(CacheLayer.walls);
                    renderer.blocks.floor.endDraw();
                });

                for(var renderer : renderer.envRenderers){
                    if((renderer.env & state.rules.environment) == renderer.env){
                        renderer.renderer.run();
                    }
                }

                if(enableDarkness) Draw.draw(Layer.darkness, renderer.blocks::drawDarkness);
                if(renderer.bloom != null){
                    renderer.bloom.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
                    Draw.draw(Layer.bullet - 0.02f, renderer.bloom::capture);
                    Draw.draw(Layer.effect + 0.02f, renderer.bloom::render);
                }

                if(renderer.animateShields && Shaders.shield != null){
                    Draw.drawRange(Layer.shields, 1f, () -> renderer.effectBuffer.begin(Color.clear), () -> {
                        renderer.effectBuffer.end();
                        Draw.blit(renderer.effectBuffer.getTexture(), Shaders.shield);
                    });

                    Draw.drawRange(Layer.buildBeam, 1f, () -> renderer.effectBuffer.begin(Color.clear), () -> {
                        renderer.effectBuffer.end();
                        Draw.blit(renderer.effectBuffer.getTexture(), Shaders.buildBeam);
                    });
                }

                renderer.blocks.drawBlocks();
                buffer.end();

                Core.camera.position.set(cx, cy);
                Core.camera.width = cw;
                Core.camera.height = ch;
                Core.camera.update();

                /*float
                    cx = Core.camera.position.x, cy = Core.camera.position.y,
                    cw = Core.camera.width, ch = Core.camera.height;

                captureWidth = Core.graphics.getWidth();
                captureHeight = Core.graphics.getHeight();

                Core.camera.width = captureWidth;
                Core.camera.height = captureHeight;
                for(int x = 0; x < width; x++){
                    for(int y = 0; y < height; y++){
                        FrameBuffer buffer = buffers[x][y];
                        if(buffer == null) continue;

                        Core.camera.position.set(data.bound.x + data.bound.width / 2f + captureWidth * x, data.bound.y + data.bound.height / 2f + captureHeight * y);
                        Core.camera.update();

                        buffer.begin(Color.clear);

                        // Surely this won't lag at all.
                        Draw.draw(Layer.floor, renderer.blocks.floor::drawFloor);
                        Draw.draw(Layer.block - 1, renderer.blocks::drawShadows);
                        Draw.draw(Layer.block - 0.09f, () -> {
                            renderer.blocks.floor.beginDraw();
                            renderer.blocks.floor.drawLayer(CacheLayer.walls);
                            renderer.blocks.floor.endDraw();
                        });

                        for(var renderer : renderer.envRenderers){
                            if((renderer.env & state.rules.environment) == renderer.env){
                                renderer.renderer.run();
                            }
                        }

                        if(enableDarkness) Draw.draw(Layer.darkness, renderer.blocks::drawDarkness);
                        if(renderer.bloom != null){
                            renderer.bloom.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
                            Draw.draw(Layer.bullet - 0.02f, renderer.bloom::capture);
                            Draw.draw(Layer.effect + 0.02f, renderer.bloom::render);
                        }

                        if(renderer.animateShields && Shaders.shield != null){
                            Draw.drawRange(Layer.shields, 1f, () -> renderer.effectBuffer.begin(Color.clear), () -> {
                                renderer.effectBuffer.end();
                                Draw.blit(renderer.effectBuffer.getTexture(), Shaders.shield);
                            });

                            Draw.drawRange(Layer.buildBeam, 1f, () -> renderer.effectBuffer.begin(Color.clear), () -> {
                                renderer.effectBuffer.end();
                                Draw.blit(renderer.effectBuffer.getTexture(), Shaders.buildBeam);
                            });
                        }

                        renderer.blocks.drawBlocks();
                        Groups.draw.draw(e -> {
                            if(e instanceof Building) e.draw();
                        });

                        buffer.end();
                    }
                }

                Core.camera.position.set(cx, cy);
                Core.camera.width = cw;
                Core.camera.height = ch;
                Core.camera.update();*/
            }

            @Override
            public void dispose(){
                buffer.dispose();
                /*for(int x = 0; x < textureWidth; x++){
                    for(int y = 0; y < textureHeight; y++){
                        if(buffers[x][y] != null) buffers[x][y].dispose();
                        buffers[x][y] = null;
                    }
                }*/
            }
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
