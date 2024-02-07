package confictura.graphics.g3d;

import arc.graphics.*;
import arc.struct.*;
import arc.util.*;
import confictura.graphics.g3d.MeshDrawer.*;
import confictura.util.*;

public class MeshDrawer<V extends Vertex>{
    protected @Nullable VertexAttribute[] attributes;
    protected final FloatSeq vertices = new FloatSeq();
    protected final ShortSeq indices = new ShortSeq();

    public MeshDrawer<V> vertex(V vertex){
        if(attributes == null) attributes = vertex.attributes().clone();
        vertices.addAll(vertex.asFloats());
        return this;
    }

    public MeshDrawer<V> vertices(V[] vertices, short[] indices){
        short base = (short)this.vertices.size;
        for(var v : vertices) vertex(v);
        for(short i : indices) this.indices.add(base + i);
        return this;
    }

    public MeshDrawer<V> indices(short[] indices){
        this.indices.addAll(indices);
        return this;
    }

    public <S extends Face<V>> MeshDrawer<V> face(S face){
        return vertices(face.vertices(), face.indices());
    }

    public Mesh build(){
        if(attributes == null) throw new IllegalStateException("Can't build a mesh without vertices.");
        var out = new Mesh(true, vertices.size * 4 / StructUtils.sumi(attributes, a -> a.size), indices.size, attributes);
        out.setVertices(vertices.items, 0, vertices.size);
        out.setIndices(indices.items, 0, indices.size);
        return out;
    }

    public static abstract class Vertex{
        public abstract float[] asFloats();

        public abstract VertexAttribute[] attributes();
    }

    public static abstract class Face<V extends Vertex>{
        public abstract V[] vertices();

        public abstract short[] indices();
    }
}
