package confictura.editor;

import arc.*;
import arc.Graphics.Cursor.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import confictura.editor.Editors.*;
import confictura.util.*;
import confictura.world.*;
import confictura.world.blocks.environment.*;
import mindustry.core.*;
import mindustry.graphics.*;

import static mindustry.Vars.*;

public class CollapserEditor extends EditorListener{
    private static final int
        chunkSize = 64,
        chunkUnits = chunkSize * tilesize,
        vertSize = 2 + 1,
        vertCount = chunkSize * chunkSize * 4,
        indCount = chunkSize * chunkSize * 6,
        maxSprites = chunkSize * chunkSize;

    private static final float pad = tilesize / 2f;

    private static final VertexAttribute[] attributes = {VertexAttribute.position, VertexAttribute.color};
    private static final float[] vertices = new float[maxSprites * vertSize * 4];
    private static final short[] indices = new short[maxSprites * 6];

    public IntGrid grid = new IntGrid(0, 0);
    public Color[] colors;

    protected Table barrier;
    protected int floorIndex;
    protected IntSeq toDraw = new IntSeq(), toUpdate = new IntSeq();
    protected IntSet toDrawUsed = new IntSet();

    protected Vec2 start = new Vec2();

    protected CMesh[] chunks;
    protected int chunkWidth, chunkHeight;
    protected Shader shader;
    protected CollapseBatch batch;
    protected int vertIndex;

    @Override
    public void register(){
        super.register();

        Core.scene.root.fill(t -> barrier = t);
        barrier.touchable = Touchable.disabled;
        barrier.toBack();
        barrier.addListener(new ClickListener(){
            int lastX, lastY;

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                if(amountY < 0f){
                    floorIndex = Math.min(CollapseFloor.max() - 1, floorIndex + 1);
                }else if(amountY > 0f){
                    floorIndex = Math.max(0, floorIndex - 1);
                }

                return false;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                if(super.touchDown(event, x, y, pointer, button)){
                    start.set(Core.input.mouseWorldX(), Core.input.mouseWorldY());

                    int tx = World.toTile(start.x), ty = World.toTile(start.y), pos = Point2.pack(tx, ty);
                    if(Structs.inBounds(tx, ty, grid.width, grid.height) && toDrawUsed.add(pos)){
                        lastX = tx;
                        lastY = ty;
                        toDraw.add(pos);
                    }

                    return true;
                }else{
                    return false;
                }
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer){
                super.touchDragged(event, x, y, pointer);

                int tx = World.toTile(Core.input.mouseWorldX()), ty = World.toTile(Core.input.mouseWorldY());
                world.raycastEach(lastX, lastY, tx, ty, (wx, wy) -> {
                    boolean inBounds = Structs.inBounds(wx, wy, grid.width, grid.height);

                    int pos = Point2.pack(wx, wy);
                    if(inBounds && toDrawUsed.add(pos)) toDraw.add(pos);

                    return !inBounds;
                });

                lastX = tx;
                lastY = ty;
            }

            @Override
            public void clicked(InputEvent event, float x, float y){
                super.clicked(event, x, y);
                for(int i = 0; i < toDraw.size; i++){
                    int tpos = toDraw.get(i),
                        tx = Point2.x(tpos), ty = Point2.y(tpos),
                        c = Point2.pack(tx / chunkSize, ty / chunkSize);

                    if(grid.get(tx, ty) != floorIndex && !toUpdate.contains(c)) toUpdate.add(c);
                    grid.set(tx, ty, floorIndex);
                }

                toDraw.clear();
                toDrawUsed.clear();
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Element fromActor){
                super.enter(event, x, y, pointer, fromActor);
                if(pointer == -1 && event.targetActor.visible) Core.graphics.cursor(SystemCursor.crosshair);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Element toActor){
                super.exit(event, x, y, pointer, toActor);
                if(pointer == -1) Core.graphics.restoreCursor();
            }
        });

        colors = new Color[CollapseFloor.max()];
        for(int i = 0; i < colors.length; i++) colors[i] = new Color(Color.red).shiftHue(255f * ((float)i / colors.length)).a(0.3f);

        shader = new Shader(
            """
            attribute vec2 a_position;
            attribute vec4 a_color;

            uniform mat4 u_proj;

            varying vec4 v_color;

            void main(){
                v_color = a_color;
                v_color.a = v_color.a * (255.0/254.0);
                gl_Position = u_proj * vec4(a_position, 0.0, 1.0);
            }
            """,
            """
            varying vec4 v_color;

            void main(){
                gl_FragColor = v_color;
            }
            """
        );

        batch = new CollapseBatch();
        for(short i = 0, j = 0; i < indices.length; i += 6, j += 4){
            indices[i] = j;
            indices[i + 1] = (short)(j + 1);
            indices[i + 2] = (short)(j + 2);
            indices[i + 3] = (short)(j + 2);
            indices[i + 4] = (short)(j + 3);
            indices[i + 5] = j;
        }
    }

    @Override
    public void begin(){
        WorldState.read(data(), grid);
        chunkWidth = Mathf.ceil((float)world.width() / chunkSize);
        chunkHeight = Mathf.ceil((float)world.height() / chunkSize);

        int len = chunkWidth * chunkHeight;
        chunks = new CMesh[len];

        for(int x = 0; x < chunkWidth; x++){
            for(int y = 0; y < chunkHeight; y++) cache(x, y);
        }
    }

    @Override
    public void end(){
        editor.tags.put(WorldState.dataKey, data(data().merge(WorldState.write(grid))));
        for(CMesh chunk : chunks) if(chunk != null) chunk.dispose();
    }

    @Override
    public void update(){
        barrier.touchable = (Core.input.ctrl() || toDraw.size > 0) ? Touchable.enabled : Touchable.disabled;
    }

    public void cache(int x, int y){
        if(!Structs.inBounds(x, y, chunkWidth, chunkHeight)) return;

        vertIndex = 0;
        Batch prev = Core.batch;
        Core.batch = batch;

        int sx = chunkSize * x, sy = chunkSize * y,
            ex = sx + chunkSize, ey = sy + chunkSize;

        for(int tx = sx; tx < ex; tx++){
            for(int ty = sy; ty < ey; ty++){
                if(!Structs.inBounds(tx, ty, grid.width, grid.height)) continue;

                int value = grid.get(tx, ty);
                if(value == -1) continue;

                Draw.color(colors[value]);
                Fill.rect(tx * tilesize, ty * tilesize, 8f, 8f);
            }
        }

        Draw.color();
        Core.batch = prev;

        CMesh prevChunk = chunks[y * chunkWidth + x];
        if(prevChunk != null) prevChunk.dispose();

        boolean use = Mesh.useVAO;
        Mesh.useVAO = false;

        CMesh mesh = new CMesh();
        mesh.count = vertIndex / vertSize / 4 * 6;
        mesh.setAutoBind(false);
        mesh.setIndices(indices, 0, indCount);
        mesh.setVertices(vertices, 0, vertIndex);

        chunks[y * chunkWidth + x] = mesh;
        Mesh.useVAO = use;
    }

    @Override
    public void draw(){
        float z = Draw.z();
        Draw.z(Layer.turret + 1f);

        if(barrier.touchable == Touchable.enabled){
            Lines.stroke(0.8f, Tmp.c1.set(colors[floorIndex]).a(1f));
            Lines.square(World.toTile(Core.input.mouseWorldX()) * tilesize, World.toTile(Core.input.mouseWorldY()) * tilesize, 4f + Mathf.sin(15f, 0.5f));
            Draw.reset();
        }

        if(toDraw.size > 0){
            Draw.color(colors[floorIndex]);
            for(int i = 0; i < toDraw.size; i++){
                int pos = toDraw.get(i), x = Point2.x(pos), y = Point2.y(pos);
                Fill.rect(x * tilesize, y * tilesize, 8f, 8f);
            }

            Draw.reset();
        }

        Draw.draw(Draw.z(), () -> {
            Draw.flush();
            if(toUpdate.size > 0){
                for(int i = 0; i < toUpdate.size; i++){
                    int tpos = toUpdate.get(i);
                    cache(Point2.x(tpos), Point2.y(tpos));
                }

                toUpdate.clear();
            }

            int
                minX = Math.max((int)((Core.camera.position.x - Core.camera.width / 2f - pad) / chunkUnits), 0),
                minY = Math.max((int)((Core.camera.position.y - Core.camera.height / 2f - pad) / chunkUnits), 0),
                maxX = Math.min(Mathf.ceil((Core.camera.position.x + Core.camera.width / 2f + pad) / chunkUnits), chunkWidth - 1),
                maxY = Math.min(Mathf.ceil((Core.camera.position.y + Core.camera.height / 2f + pad) / chunkUnits), chunkWidth - 1);

            shader.bind();
            shader.setUniformMatrix4("u_proj", Core.camera.mat);
            for(VertexAttribute attrib : attributes) shader.enableVertexAttribute(attrib.alias);

            Draw.flush();
            Blending.normal.apply();
            for(int y = minY; y <= maxY; y++){
                for(int x = minX; x <= maxX; x++){
                    CMesh chunk = chunks[y * chunkWidth + x];
                    if(chunk == null || chunk.count == 0) continue;

                    if(chunk.vertices instanceof VertexBufferObject vbo && chunk.indices instanceof IndexBufferObject ibo){
                        vbo.bind();
                        int offset = 0;
                        for(VertexAttribute attribute : attributes){
                            int location = shader.getAttributeLocation(attribute.alias);
                            if(location < 0) continue;

                            shader.setVertexAttribute(location, attribute.components, attribute.type, attribute.normalized, vertSize * 4, offset);
                            offset += attribute.size;
                        }

                        ibo.bind();
                        ibo.buffer().clear();
                        vbo.render(ibo, Gl.triangles, 0, chunk.count);
                    }else{
                        throw new IllegalStateException("Non-VBO meshes are not supported for caches.");
                    }
                }
            }

            for(VertexAttribute attrib : attributes) shader.disableVertexAttribute(attrib.alias);
            Gl.bindBuffer(Gl.arrayBuffer, 0);
            Gl.bindBuffer(Gl.elementArrayBuffer, 0);
        });

        Draw.z(z);
    }

    protected class CollapseBatch extends Batch{
        @Override
        protected void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation){
            float[] verts = vertices;
            int idx = vertIndex;
            vertIndex += vertSize * 4;

            float color = this.colorPacked;

            verts[idx] = x;
            verts[idx + 1] = y;
            verts[idx + 2] = color;

            verts[idx + 3] = x;
            verts[idx + 4] = y + height;
            verts[idx + 5] = color;

            verts[idx + 6] = x + width;
            verts[idx + 7] = y + height;
            verts[idx + 8] = color;

            verts[idx + 9] = x + width;
            verts[idx + 10] = y;
            verts[idx + 11] = color;
        }

        @Override
        public void flush(){}

        @Override
        public void setShader(Shader shader, boolean apply){
            throw new IllegalStateException("Cache shader unsupported.");
        }

        @Override
        protected void draw(Texture texture, float[] spriteVertices, int offset, int count){
            throw new IllegalStateException("Cache vertices unsupported.");
        }
    }

    protected static class CMesh extends Mesh{
        protected int count;

        protected CMesh(){
            super(false, false, vertCount, indCount, CollapserEditor.attributes);
        }
    }
}
