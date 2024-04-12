package confictura.graphics.g3d;

import arc.graphics.*;
import arc.graphics.g3d.*;
import arc.math.geom.*;

/**
 * Provides convenient extension methods for things like {@link VertexBatch3D}.
 * @author GlFolker
 */
public final class Draw3DUtils{
    private static final Vec3 a = new Vec3(), b = new Vec3(), c = new Vec3(), d = new Vec3();

    private Draw3DUtils(){
        throw new AssertionError();
    }

    public static void quad(
        VertexBatch3D batch,
        Vec3 p1, Vec3 n1, Color c1, float u1, float v1,
        Vec3 p2, Vec3 n2, Color c2, float u2, float v2,
        Vec3 p3, Vec3 n3, Color c3, float u3, float v3,
        Vec3 p4, Vec3 n4, Color c4, float u4, float v4
    ){
        batch.normal(n1);
        batch.color(c1);
        batch.texCoord(u1, v1);
        batch.vertex(p1);

        batch.normal(n2);
        batch.color(c2);
        batch.texCoord(u2, v2);
        batch.vertex(p2);

        batch.normal(n3);
        batch.color(c3);
        batch.texCoord(u3, v3);
        batch.vertex(p3);

        batch.normal(n3);
        batch.color(c3);
        batch.texCoord(u3, v3);
        batch.vertex(p3);

        batch.normal(n4);
        batch.color(c4);
        batch.texCoord(u4, v4);
        batch.vertex(p4);

        batch.normal(n1);
        batch.color(c1);
        batch.texCoord(u1, v1);
        batch.vertex(p1);
    }

    public static void quad2(
        VertexBatch3D batch,
        Vec3 p1, Vec3 n1, Color c1, float u1, float v1,
        Vec3 p2, Vec3 n2, Color c2, float u2, float v2,
        Vec3 p3, Vec3 n3, Color c3, float u3, float v3,
        Vec3 p4, Vec3 n4, Color c4, float u4, float v4
    ){
        quad(
            batch,
            p1, n1, c1, u1, v1,
            p2, n2, c2, u2, v2,
            p3, n3, c3, u3, v3,
            p4, n4, c4, u4, v4
        );

        quad(
            batch,
            p4, a.set(n4).scl(-1f), c4, u4, v4,
            p3, b.set(n3).scl(-1f), c3, u3, v3,
            p2, c.set(n2).scl(-1f), c2, u2, v2,
            p1, d.set(n1).scl(-1f), c1, u1, v1
        );
    }
}
