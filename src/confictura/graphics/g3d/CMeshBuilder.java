package confictura.graphics.g3d;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import confictura.graphics.g3d.CMeshBuilder.IslandShaper.*;

/**
 * Utilities for composing specialized OpenGL {@link Mesh meshes}.
 * @author GlennFolker
 */
public final class CMeshBuilder{
    private static Mesh mesh;
    private static final float[] vert = new float[3 + 3 + 1];
    private static final Vec3 v1 = new Vec3(), v2 = new Vec3(), nor = new Vec3();

    private static final Point2[] hexNeighbors = {
        new Point2(1, -1),
        new Point2(1, 0),
        new Point2(0, 1),
        new Point2(-1, 0),
        new Point2(-1, -1),
        new Point2(0, -1)
    };

    private CMeshBuilder(){
        throw new AssertionError();
    }

    public static Mesh island(float radius, float resolution, IslandShaper shaper){
        int segments = Math.max(1, Mathf.round(radius * resolution));
        float centerLeap = radius / (segments - 1);
        float xLeap = centerLeap * 0.5f * Mathf.sqrt3;

        int height = segments * 2 - 1;
        int width = Mathf.round((centerLeap / xLeap) * height);
        float vertLeap = (centerLeap * 0.5f) / (0.5f * Mathf.sqrt3);

        var shapes = new Hex[width][height];

        int hexCount = 0;
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                var hex = shapes[x][y] = new Hex();
                hex.x = (x - (width % 2 == 1 ? (float)(width / 2) : (width / 2f + 0.5f))) * xLeap;
                hex.y = (y - (float)(height / 2)) * centerLeap - (1 - x % 2) * centerLeap * 0.5f;

                shaper.get(hex.x, hex.y, hex);
                if(hex.low > hex.high){
                    shapes[x][y] = null;
                }else{
                    hexCount += 1;
                }
            }
        }

        class Tile{
            final Hex hex;
            final Vec3[] lows = new Vec3[hexNeighbors.length];
            final Vec3[] highs = new Vec3[hexNeighbors.length];

            Tile(Hex hex){
                this.hex = hex;
            }
        }

        Seq<Tile> tiles = new Seq<>();
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                var hex = shapes[x][y];
                if(hex == null) continue;

                var tile = new Tile(hex);
                for(int i = 0; i < hexNeighbors.length; i++){
                    int p1x, p1y, p2x, p2y;
                    {
                        Point2 p1 = hexNeighbors[i], p2 = hexNeighbors[(i + 1) % hexNeighbors.length];
                        p1x = p1.x;
                        p1y = p1.y;

                        p2x = p2.x;
                        p2y = p2.y;

                        if(x % 2 == 1){
                            if(p1x != 0) p1y += 1;
                            if(p2x != 0) p2y += 1;
                        }
                    }

                    Hex n1 = Hex.get(x + p1x, y + p1y, width, height, shapes),
                        n2 = Hex.get(x + p2x, y + p2y, width, height, shapes);

                    var low = tile.lows[i] = new Vec3();
                    low.set(hex.x + Mathf.cosDeg(60f * i) * vertLeap, hex.y + Mathf.sinDeg(60f * i) * vertLeap, 0f);

                    if(n1 == null && n2 == null){
                        low.z = (hex.low + hex.high) / 2f;
                    }else if(n1 == null){
                        low.z = (hex.low + hex.high + n2.low + n2.high) / 4f;
                    }else if(n2 == null){
                        low.z = (hex.low + hex.high + n1.low + n1.high) / 4f;
                    }else{
                        low.z = (hex.low + n1.low + n2.low) / 3f;
                    }

                    var high = tile.highs[i] = new Vec3();
                    high.set(hex.x + Mathf.cosDeg(60f * i) * vertLeap, hex.y + Mathf.sinDeg(60f * i) * vertLeap, 0f);

                    if(n1 == null && n2 == null){
                        high.z = (hex.high + hex.low) / 2f;
                    }else if(n1 == null){
                        high.z = (hex.high + hex.low + n2.high + n2.low) / 4f;
                    }else if(n2 == null){
                        high.z = (hex.high + hex.low + n1.high + n1.low) / 4f;
                    }else{
                        high.z = (hex.high + n1.high + n2.high) / 3f;
                    }

                    yz(low);
                    yz(high);
                }

                tiles.add(tile);
            }
        }

        begin(hexCount * 2 * 6 * 3);
        for(var tile : tiles){
            Vec3[] l = tile.lows, h = tile.highs;
            v1.set(normal(l[0], l[2], l[4]));
            v2.set(normal(l[1], l[3], l[5]));

            nor.set(v1).add(v2).nor();
            yz(v1.set(tile.hex.x, tile.hex.y, tile.hex.low));
            for(int i = 0, len = l.length; i < len; i++) verts(v1, l[i], l[(i + 1) % len], nor, tile.hex.lowColor);

            v1.set(normal(h[0], h[4], h[2]));
            v2.set(normal(h[1], h[5], h[2]));

            nor.set(v1).add(v2).nor();
            yz(v1.set(tile.hex.x, tile.hex.y, tile.hex.high));
            for(int i = 0, len = h.length; i < len; i++) verts(v1, h[(i + 1) % len], h[i], nor, tile.hex.highColor);
        }

        return end();
    }

    private static void begin(int maxVertices){
        mesh = new Mesh(
            true, maxVertices, 0,
            VertexAttribute.position3, VertexAttribute.normal, VertexAttribute.color
        );

        var buffer = mesh.getVerticesBuffer();
        buffer.limit(mesh.getMaxVertices() * mesh.vertexSize / 4);
        buffer.position(0);
    }

    private static Mesh end(){
        var last = mesh;
        var buffer = mesh.getVerticesBuffer();
        buffer.limit(buffer.position());

        mesh = null;
        return last;
    }

    private static Vec3 yz(Vec3 in){
        float z = in.z;
        in.z = in.y;
        in.y = z;
        return in;
    }

    private static void verts(Vec3 a, Vec3 b, Vec3 c, Vec3 normal, Color color){
        vert(a, normal, color);
        vert(b, normal, color);
        vert(c, normal, color);
    }

    private static void vert(Vec3 vertex, Vec3 normal, Color color){
        vert[0] = vertex.x;
        vert[1] = vertex.y;
        vert[2] = vertex.z;

        vert[3] = normal.x;
        vert[4] = normal.y;
        vert[5] = normal.z;

        vert[6] = color.toFloatBits();
        mesh.getVerticesBuffer().put(vert);
    }

    private static Vec3 normal(Vec3 a, Vec3 b, Vec3 c){
        return nor.set(b).sub(a).crs(c.x - a.x, c.y - a.y, c.z - a.z).nor();
    }

    public interface IslandShaper{
        void get(float x, float y, Hex out);

        class Hex{
            public float low, high;
            public Color lowColor = new Color(), highColor = new Color();

            private float x, y;

            private static Hex get(int x, int y, int width, int height, Hex[][] shape){
                if(x >= 0 && y >= 0 && x < width && y < height){
                    return shape[x][y];
                }else{
                    return null;
                }
            }
        }
    }
}
