package confictura.graphics.g3d;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import confictura.graphics.g3d.CMeshBuilder.IslandShaper.*;
import confictura.world.celestial.mesh.DualHexMesh.*;
import mindustry.graphics.g3d.*;
import mindustry.graphics.g3d.PlanetGrid.*;

import static confictura.util.MathUtils.*;
import static confictura.util.StructUtils.*;

/**
 * Utilities for composing specialized OpenGL {@linkplain Mesh meshes}.
 * @author GlFolker
 */
public final class CMeshBuilder{
    private static Mesh mesh;
    private static final float[] vert = new float[3 + 3 + 1];
    private static final Vec3 v1 = new Vec3(), v2 = new Vec3(), v3 = new Vec3(), nor = new Vec3();
    private static final Color c1 = new Color(), c2 = new Color(), c3 = new Color();

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
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                var hex = shapes[x][y] = new Hex();
                hex.x = (x - (width % 2 == 1 ? (float)(width / 2) : (width / 2f + 0.5f))) * xLeap;
                hex.y = (y - (float)(height / 2)) * centerLeap - (1 - x % 2) * centerLeap * 0.5f;

                shaper.get(hex.x, hex.y, hex);
                if(hex.low > hex.high) shapes[x][y] = null;
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
                for(int i = 0, len = hexNeighbors.length; i < len; i++){
                    int p1x, p1y, p2x, p2y;
                    {
                        Point2 p1 = hexNeighbors[i], p2 = hexNeighbors[(i + 1) % len];
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

        begin(tiles.size * 2 * 6 * 3);
        for(var tile : tiles){
            Vec3[] l = tile.lows, h = tile.highs;
            v1.set(normal(nor, l[0], l[2], l[4]));
            v2.set(normal(nor, l[1], l[3], l[5]));

            nor.set(v1).add(v2).nor();
            yz(v1.set(tile.hex.x, tile.hex.y, tile.hex.low));
            for(int i = 0, len = l.length; i < len; i++) verts(v1, l[i], l[(i + 1) % len], nor, tile.hex.lowColor);

            v1.set(normal(nor, h[0], h[4], h[2]));
            v2.set(normal(nor, h[1], h[5], h[2]));

            nor.set(v1).add(v2).nor();
            yz(v1.set(tile.hex.x, tile.hex.y, tile.hex.high));
            for(int i = 0, len = h.length; i < len; i++) verts(v1, h[(i + 1) % len], h[i], nor, tile.hex.highColor);
        }

        return end();
    }

    public static Mesh gridLines(PlanetGrid grid, Color color){
        begin(grid.edges.length * 2);
        for(var edge : grid.edges){
            vert(edge.corners[0].v, nor.setZero(), color);
            vert(edge.corners[1].v, nor.setZero(), color);
        }

        return end();
    }

    public static Mesh gridDistance(PlanetGrid grid, Color color, float radius){
        return gridDistance(grid, new HexMesher(){
            @Override
            public float getHeight(Vec3 position){
                return 0f;
            }

            @Override
            public Color getColor(Vec3 position){
                return color;
            }
        }, radius, 0f);
    }

    public static Mesh gridDistance(PlanetGrid grid, HexMesher mesher, float radius, float intensity){
        int totalVerts = sumi(grid.tiles, tile -> mesher.skip(tile.v) ? 0 : tile.corners.length) * 3;
        float[] progress = new float[grid.tiles.length];

        int accum = 0;
        Seq<Ptile> queue = new Seq<>(), process = new Seq<>();
        var processed = new IntSet();

        queue.add(grid.tiles[0]);
        processed.add(0);

        while(!queue.isEmpty()){
            for(var tile : queue){
                for(var n : tile.tiles) if(processed.add(n.id)) process.add(n);
                progress[tile.id] = accum;
            }

            queue.set(process);
            process.clear();
            accum++;
        }

        for(int i = 0; i < progress.length; i++) progress[i] /= accum;

        begin(totalVerts);
        for(var tile : grid.tiles){
            if(mesher.skip(tile.v)) continue;
            v1.setZero();

            var c = tile.corners;
            for(var corner : c){
                corner.v.setLength((1f + mesher.getHeight(v2.set(corner.v)) * intensity) * radius);
                v1.add(corner.v);
            }

            v1.scl(1f / c.length);
            c1.set(mesher.getColor(tile.v)).a(1f);

            for(int i = 0, len = c.length; i < len; i++){
                Corner a = c[i], b = c[(i + 1) % len];
                c2.set(mesher.getColor(v2.set(a.v).nor())).a(0f);
                c3.set(mesher.getColor(v3.set(b.v).nor())).a(0f);

                vert(v1, nor.set(progress[tile.id], 0f, 0f), c1);
                vert(a.v, nor.set(average(a.tiles, t -> progress[t.id]), 0f, 0f), c2);
                vert(b.v, nor.set(average(b.tiles, t -> progress[t.id]), 0f, 0f), c3);
            }

            for(var corner : c) corner.v.nor();
        }

        return end();
    }

    public static Mesh buildDualHex(DualHexMesher mesher, int divisions, float radius){
        var grid = PlanetGrid.create(divisions);

        var topology = new Vec2();
        int tl = grid.tiles.length, cl = grid.corners.length;
        float[] tileHeights = new float[tl * 2];
        float[] cornerHeights = new float[cl * 2];

        int count = 0;
        for(var tile : grid.tiles){
            mesher.topology(tile.v, topology);
            if((tileHeights[tile.id] = topology.x) >= (tileHeights[tl + tile.id] = topology.y)){
                count += 3 * tile.corners.length;
            }
        }

        outer:
        for(var c : grid.corners){
            mesher.topology(c.v, topology);
            for(var t : c.tiles){
                if(tileHeights[t.id] < tileHeights[tl + t.id]){
                    cornerHeights[c.id] = cornerHeights[cl + c.id] = (topology.x + topology.y) / 2f;
                    continue outer;
                }
            }

            cornerHeights[c.id] = topology.x;
            cornerHeights[cl + c.id] = topology.y;
        }

        begin(count * 2);
        for(var t : grid.tiles){
            if(tileHeights[t.id] < tileHeights[tl + t.id]) continue;

            Color colHigh = c1, colLow = c2;
            mesher.color(t.v, colHigh, colLow);

            var c = t.corners;
            v1.set(c[0].v).setLength(cornerHeights[c[0].id] * radius);
            v2.set(c[2].v).setLength(cornerHeights[c[2].id] * radius);
            v3.set(c[4].v).setLength(cornerHeights[c[4].id] * radius);

            var n = normal(nor, v1, v2, v3);

            v1.set(t.v).setLength(tileHeights[t.id] * radius);
            for(int i = 0, len = c.length; i < len; i++){
                Corner c1 = c[i], c2 = c[(i + 1) % len];
                v2.set(c1.v).setLength(cornerHeights[c1.id] * radius);
                v3.set(c2.v).setLength(cornerHeights[c2.id] * radius);

                verts(v1, v2, v3, n, colHigh);
            }

            v1.set(c[4].v).setLength(cornerHeights[cl + c[4].id] * radius);
            v2.set(c[2].v).setLength(cornerHeights[cl + c[2].id] * radius);
            v3.set(c[0].v).setLength(cornerHeights[cl + c[0].id] * radius);

            n = normal(nor, v1, v2, v3);

            v1.set(t.v).setLength(tileHeights[tl + t.id] * radius);
            for(int i = 0, len = c.length; i < len; i++){
                Corner c1 = c[i], c2 = c[(i + 1) % len];
                v2.set(c1.v).setLength(cornerHeights[cl + c1.id] * radius);
                v3.set(c2.v).setLength(cornerHeights[cl + c2.id] * radius);

                verts(v1, v3, v2, n, colLow);
            }
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
        buffer.position(0);

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
