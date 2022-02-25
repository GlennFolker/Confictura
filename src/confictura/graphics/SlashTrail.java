package confictura.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import confictura.assets.*;
import mindustry.graphics.*;

/**
 * A shader-ed trail; mostly copy-pasted from Project Unity's textured trail.
 * @author GlennFolker
 */
public class SlashTrail extends Trail{
    /** The trail's width shrink as it goes, in percentage. `1f` makes the trail triangle-shaped. */
    public float shrink = 0f;
    /** The perlin noise scale for distortion. */
    public float noiseScale = 160f;

    private static final float[] vertices = new float[24];

    // sigh.
    protected final FloatSeq points;
    protected float lastX = -1f, lastY = -1f, lastAngle = -1f, lastSlashAngle = -1f, counter = 0f, lastW = 0f;

    public SlashTrail(int length){
        super(0); // Don't allocate anything for base class' point array.

        this.length = length;
        points = new FloatSeq(length * 4);
    }

    @Override
    public SlashTrail copy(){
        SlashTrail out = new SlashTrail(length);
        out.shrink = shrink;
        out.noiseScale = noiseScale;
        out.points.addAll(points);
        out.lastX = lastX;
        out.lastY = lastY;
        out.lastAngle = lastAngle;

        return out;
    }

    @Override
    public void clear(){
        points.clear();
    }

    @Override
    public int size(){
        return points.size / 4;
    }

    @Override
    public void drawCap(Color color, float width){
        //TODO
        /*int psize = points.size;
        if(psize > 0){
            float[] items = points.items;

            int i = psize - 4;
            float x1 = items[i], y1 = items[i + 1], w1 = items[i + 2], w = w1 * width / (psize / 4f) * i / 4f * 2f;
            if(w1 <= 0.001f) return;

            Draw.blend(blend);
            Draw.mixcol(color, mixAlpha);

            Draw.rect(capRegion, x1, y1, w, w, -Mathf.radDeg * lastAngle + 90f);

            Draw.mixcol();
            Draw.blend();
        }*/
    }

    @Override
    public void draw(Color color, float width){
        if(points.isEmpty()) return;
        TextureRegion region = Core.atlas.white();

        float[] items = points.items;
        int psize = points.size;

        float
            endAngle = lastAngle,
            size = width / (psize / 4f), fracOffset = psize - psize * shrink, fracStride = 1f - fracOffset / psize,
            u = region.u2, v = region.v, u2 = region.u, v2 = region.v2;

        Draw.draw(Draw.z(), () -> {
            CShaders.slash.noiseScale = noiseScale;

            Draw.shader(CShaders.slash);
            Draw.blend(Blending.disabled);

            float lastAngle = endAngle;
            for(int i = 0; i < psize; i += 4){
                float
                    x1 = items[i], y1 = items[i + 1], w1 = items[i + 2], rv1 = items[i + 3],
                    x2, y2, w2, rv2;

                if(i < psize - 4){
                    x2 = items[i + 4];
                    y2 = items[i + 5];
                    w2 = items[i + 6];
                    rv2 = items[i + 7];
                }else{
                    x2 = lastX;
                    y2 = lastY;
                    w2 = lastW;
                    rv2 = 1f;
                }

                float
                    z2 = i == psize - 4 ? endAngle : -Angles.angleRad(x1, y1, x2, y2), z1 = i == 0 ? z2 : lastAngle,
                    slashAngle = (i == psize - 4 ? lastSlashAngle : -Angles.angleRad(x1, y1, x2, y2)) / Mathf.PI2,
                    fs1 = ((fracOffset + i * fracStride) / 4f) * size * w1,
                    fs2 = ((fracOffset + (i + 4f) * fracStride) / 4f) * size * w2,

                    cx = Mathf.sin(z1) * fs1, cy = Mathf.cos(z1) * fs1,
                    nx = Mathf.sin(z2) * fs2, ny = Mathf.cos(z2) * fs2,

                    mv1 = Mathf.lerp(v2, v, rv1), mv2 = Mathf.lerp(v2, v, rv2),
                    slash1 = Interp.pow3Out.apply(rv1), slash2 = Interp.pow3Out.apply(rv2),
                    col1 = Tmp.c1.set(color).a(Tmp.c1.a * rv1).toFloatBits(),
                    col2 = Tmp.c1.set(color).a(Tmp.c1.a * rv2).toFloatBits();

                vertices[0] = x1 - cx;
                vertices[1] = y1 - cy;
                vertices[2] = Tmp.c1.set(slashAngle, 0f, slash1).toFloatBits();
                vertices[3] = u;
                vertices[4] = mv1;
                vertices[5] = col1;

                vertices[6] = x1 + cx;
                vertices[7] = y1 + cy;
                vertices[8] = Tmp.c1.set(slashAngle, 1f, slash1).toFloatBits();
                vertices[9] = u2;
                vertices[10] = mv1;
                vertices[11] = col1;

                vertices[12] = x2 + nx;
                vertices[13] = y2 + ny;
                vertices[14] = Tmp.c1.set(slashAngle, 1f, slash2).toFloatBits();
                vertices[15] = u2;
                vertices[16] = mv2;
                vertices[17] = col2;

                vertices[18] = x2 - nx;
                vertices[19] = y2 - ny;
                vertices[20] = Tmp.c1.set(slashAngle, 0f, slash2).toFloatBits();
                vertices[21] = u;
                vertices[22] = mv2;
                vertices[23] = col2;

                Draw.vert(region.texture, vertices, 0, 24);
                lastAngle = z2;
            }

            Draw.blend();
            Draw.shader();
        });
    }

    @Override
    public void shorten(){
        if((counter += Time.delta) >= 1f){
            if(points.size >= 4) points.removeRange(0, 3);
            counter %= 1f;
        }

        calcProgress();
    }

    @Override
    public void update(float x, float y, float width){
        if((counter += Time.delta) >= 1f){
            if(points.size > length * 4) points.removeRange(0, 3);

            counter %= 1f;
            points.add(x, y, width, 0f);
        }

        lastAngle = -Angles.angleRad(x, y, lastX, lastY);
        lastSlashAngle = -Angles.angleRad(lastX, lastY, x, y);
        lastX = x;
        lastY = y;
        lastW = width;

        calcProgress();
    }

    public void calcProgress(){
        int psize = points.size;
        if(psize > 0){
            float[] items = points.items;

            float maxDist = 0f;
            for(int i = 0; i < psize; i += 4){
                float
                    x1 = items[i], y1 = items[i + 1],
                    dst = i < psize - 4 ? Mathf.dst(x1, y1, items[i + 4], items[i + 5]) : Mathf.dst(x1, y1, lastX, lastY);

                maxDist += dst;
                items[i + 3] = dst;
            }

            float frac = length / (points.size / 4f);
            float first = items[3];

            float last = 0f;
            for(int i = 0; i < psize; i += 4){
                float v = items[i + 3];

                items[i + 3] = (v + last - first) / maxDist * frac;
                last += v;
            }
        }
    }
}
