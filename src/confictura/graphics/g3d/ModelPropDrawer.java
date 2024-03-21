package confictura.graphics.g3d;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g3d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.pooling.*;
import confictura.graphics.shaders.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;

import static arc.Core.*;
import static confictura.util.StructUtils.*;
import static mindustry.Vars.*;

public class ModelPropDrawer implements Disposable{
    public static final VertexAttribute[] attributes = {new VertexAttribute(4, Gl.unsignedShort, true, "a_color"), VertexAttribute.position3, VertexAttribute.normal};
    public static final int attribStride = sumi(attributes, attrib -> attrib.size / Float.BYTES);
    public static final Boolf2<VertexAttribute, VertexAttribute> attribEq = (a, b) ->
        a.components == b.components &&
        a.normalized == b.normalized &&
        a.type == b.type &&
        a.alias.equals(b.alias) &&
        a.size == b.size;

    public static final float flushLayer = Layer.flyingUnitLow - 1f;
    public static final float accumLayer = flushLayer - 0.01f;

    protected static final Pool<Req> pool = Pools.get(Req.class, Req::new);
    private static final Vec3 pos = new Vec3(), scl = new Vec3(), vec = new Vec3();
    private static final Quat quat = new Quat();
    private static final Mat3D nor = new Mat3D();

    protected ObjectMap<Mesh, PropData> data = new ObjectMap<>();
    protected Camera3D cam = new Camera3D(){{
        fov = 60f;
        near = 1f;

        up.set(0f, 0f, -1f);
        direction.set(0f, -1f, 0f);
    }};
    protected FrameBuffer buffer = new FrameBuffer(2, 2, true);
    protected Seq<Req> requests = new Seq<>();

    protected Mesh batch;
    protected ModelPropShader shader;
    protected float[] vertices;
    protected short[] indices;
    protected int vertexOffset, indexOffset;

    public ModelPropDrawer(ModelPropShader shader, int maxVertices, int maxIndices){
        batch = new Mesh(false, maxVertices, maxIndices, attributes);
        this.shader = shader;
        vertices = new float[maxVertices * batch.vertexSize];
        indices = new short[maxIndices];

        Events.run(Trigger.drawOver, () -> Draw.draw(flushLayer, this::render));
    }

    public PropData getData(Mesh mesh){
        return data.get(mesh, () -> {
            if(!arrayEq(mesh.attributes, attributes, attribEq)) throw new IllegalArgumentException("Mesh must only have color, 3D position, and normals.");

            var vertices = mesh.getVerticesBuffer().duplicate();
            var indices = mesh.getIndicesBuffer().duplicate();

            var out = new PropData(new float[vertices.remaining()], new short[indices.remaining()]);
            vertices.get(out.vertices);
            indices.get(out.indices);
            return out;
        });
    }

    public void draw(Mesh mesh, float x, float y, float rotation){
        var req = pool.obtain();
        req.trns.set(pos.set(x, 0f, -y), quat.setFromAxis(Vec3.Y, rotation), scl.set(tilesize, tilesize, tilesize).scl(0.5f));
        req.data = getData(mesh);
        requests.add(req);
    }

    public void render(){
        if(requests.isEmpty()) return;
        int sw = graphics.getWidth(), sh = graphics.getHeight();

        cam.resize(sw, sh);
        cam.position.set(camera.position.x, camera.height / 2f / Mathf.tan(cam.fov / 2f * Mathf.degRad, 1f, 1f), -camera.position.y);
        cam.far = Math.max(150f, cam.position.y * 1.5f);
        cam.update();

        buffer.resize(sw, sh);
        buffer.begin();

        Gl.enable(Gl.depthTest);
        Gl.depthMask(true);

        Gl.enable(Gl.cullFace);
        Gl.cullFace(Gl.back);

        Gl.clearColor(0f, 0f, 0f, 0f);
        Gl.clear(Gl.colorBufferBit | Gl.depthBufferBit);

        for(var req : requests){
            var trns = req.trns;
            var data = req.data;

            var input = data.vertices;
            if(input.length >= vertices.length - vertexOffset * attribStride || data.indices.length >= indices.length - indexOffset) flush();

            int offset = vertexOffset * attribStride;
            for(int i = 0; i < input.length; i += attribStride){
                int index = offset + i;
                System.arraycopy(input, i, vertices, index, Short.BYTES * 4 / Float.BYTES);

                var p = Mat3D.prj(vec.set(input[i + 2], input[i + 3], input[i + 4]), trns);
                vertices[index + 2] = p.x;
                vertices[index + 3] = p.y;
                vertices[index + 4] = p.z;

                var n = Mat3D.prj(vec.set(input[i + 5], input[i + 6], input[i + 7]), nor.set(trns).toNormalMatrix()).nor();
                vertices[index + 5] = n.x;
                vertices[index + 6] = n.y;
                vertices[index + 7] = n.z;
            }

            for(short index : data.indices) indices[indexOffset++] = (short)(vertexOffset + index);
            vertexOffset += input.length / attribStride;
        }

        flush();
        buffer.end();

        Gl.depthMask(false);
        Gl.disable(Gl.cullFace);
        Gl.disable(Gl.depthTest);

        buffer.blit(Shaders.screenspace);

        pool.freeAll(requests);
        requests.clear();
    }

    protected void flush(){
        if(indexOffset == 0) return;

        shader.bind();
        shader.camera = cam;
        shader.lightDir.set(-1f, -1f, 1f).nor();
        shader.apply();

        batch.setVertices(vertices, 0, vertexOffset * attribStride);
        batch.setIndices(indices, 0, indexOffset);
        batch.render(shader, Gl.triangles, 0, indexOffset);

        vertexOffset = indexOffset = 0;
    }

    @Override
    public void dispose(){
        pool.freeAll(requests);
        requests.clear();
        batch.dispose();
    }

    public static class PropData{
        public float[] vertices;
        public short[] indices;

        public PropData(float[] vertices, short[] indices){
            this.vertices = vertices;
            this.indices = indices;
        }
    }

    public static class Req{
        public Mat3D trns = new Mat3D();
        public PropData data;
    }
}
