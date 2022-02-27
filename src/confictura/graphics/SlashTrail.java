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
 * A shader-ed trail, giving a slash effect. Based around Project Unity's textured trail.
 * @author GlennFolker
 */
public class SlashTrail extends Trail{
    /** The texture region of this trail. */
    public TextureRegion region;
    /** The trail's width shrink as it goes, in percentage. `1f` makes the trail triangle-shaped. */
    public float shrink = 0f;
    /** The trail's alpha as it goes, in percentage. `1f` makes the trail's tail completely invisible. */
    public float fadeAlpha = 1f;
    /** The trail's mix color alpha, used in {@link #draw(Color, float)}. Fades as the trail goes. */
    public float mixAlpha = 0.5f;
    /** The slashing intensity scale to the trail velocity. */
    public float slashScale = 7f;
    /** The slashing noise octaves. */
    public int noiseOctaves = 4;
    /** The slashing noise scale. */
    public float noiseScale = 24.0f;
    /** The slashing noise lacunarity. */
    public float noiseLacunarity = 0.67f;
    /** The slashing noise persistence. */
    public float noisePersistence = 0.67f;
    /** The slashing noise magnitude. */
    public float noiseMagnitude = 0.3f;
    /** The trail blending. `0f` is identical to {@link Blending#normal}, and `1f` to {@link Blending#additive}. */
    public float blend = 0.5f;

    private static final float[] vertices = new float[24];

    // sigh.
    protected final FloatSeq points;
    protected float lastX = -1f, lastY = -1f, lastAngle = -1f, lastIntensity = 0f, counter = 0f, lastW = 0f;

    public SlashTrail(TextureRegion region, int length){
        this(length);
        this.region = region;
    }

    public SlashTrail(int length){
        super(0); // Don't allocate anything for base class' point array.

        this.length = length;
        points = new FloatSeq(length * 5);
    }

    @Override
    public SlashTrail copy(){
        SlashTrail out = new SlashTrail(region, length);
        out.shrink = shrink;
        out.fadeAlpha = fadeAlpha;
        out.mixAlpha = mixAlpha;
        out.slashScale = slashScale;
        out.noiseOctaves = noiseOctaves;
        out.noiseScale = noiseScale;
        out.noiseLacunarity = noiseLacunarity;
        out.noisePersistence = noisePersistence;
        out.noiseMagnitude = noiseMagnitude;
        out.blend = blend;
        out.points.addAll(points);
        out.lastX = lastX;
        out.lastY = lastY;
        out.lastW = lastW;
        out.lastAngle = lastAngle;
        out.lastIntensity = lastIntensity;

        return out;
    }

    @Override
    public void clear(){
        points.clear();
    }

    @Override
    public int size(){
        return points.size / 5;
    }

    @Override
    public void drawCap(Color color, float width){
        // Do nothing.
    }

    @Override
    public void draw(Color color, float width){
        if(region == null) region = Core.atlas.find("white");
        if(points.isEmpty()) return;

        float[] items = points.items;
        int psize = points.size;

        float
            endAngle = this.lastAngle,
            size = width / (psize / 5f), fracOffset = psize - psize * shrink, fracStride = 1f - fracOffset / psize,
            u = region.u2, v = region.v, u2 = region.u, v2 = region.v2;

        Draw.draw(Draw.z(), () -> {
            CShaders.subBuffer.begin(Color.clear);

            Draw.blend();
            Draw.shader(CShaders.slash.getShader(), true);

            CShaders.slash.begin();
            CShaders.slash.noiseOctaves = noiseOctaves;
            CShaders.slash.noiseScale = noiseScale;
            CShaders.slash.noiseLacunarity = noiseLacunarity;
            CShaders.slash.noisePersistence = noisePersistence;
            CShaders.slash.noiseMagnitude = noiseMagnitude;
            CShaders.slash.blend = blend;

            float lastAngle = endAngle;
            for(int i = 0; i < psize; i += 5){
                float
                    x1 = items[i], y1 = items[i + 1], w1 = items[i + 2], rv1 = items[i + 3], in1 = items[i + 4],
                    x2, y2, w2, rv2, in2;

                if(i < psize - 5){
                    x2 = items[i + 5];
                    y2 = items[i + 6];
                    w2 = items[i + 7];
                    rv2 = items[i + 8];
                    in2 = items[i + 9];
                }else{
                    x2 = lastX;
                    y2 = lastY;
                    w2 = lastW;
                    rv2 = 1f;
                    in2 = lastIntensity;
                }

                float
                    z2 = i == psize - 5 ? endAngle : -Angles.angleRad(x1, y1, x2, y2), z1 = i == 0 ? z2 : lastAngle,
                    fs1 = ((fracOffset + i * fracStride) / 5f) * size * w1,
                    fs2 = ((fracOffset + (i + 5f) * fracStride) / 5f) * size * w2,

                    cx = Mathf.sin(z1) * fs1, cy = Mathf.cos(z1) * fs1,
                    nx = Mathf.sin(z2) * fs2, ny = Mathf.cos(z2) * fs2,

                    mv1 = Mathf.lerp(v2, v, rv1), mv2 = Mathf.lerp(v2, v, rv2),
                    col1 = Tmp.c1.set(Draw.getColor()).a(rv1 * fadeAlpha + (1f - fadeAlpha)).toFloatBits(),
                    col2 = Tmp.c1.set(Draw.getColor()).a(rv2 * fadeAlpha + (1f - fadeAlpha)).toFloatBits(),
                    mix1 = Tmp.c1.set(color).a(rv1 * mixAlpha).toFloatBits(),
                    mix2 = Tmp.c1.set(color).a(rv2 * mixAlpha).toFloatBits();
                int index = i / 5;

                CShaders.slash.uniform(Angles.angleRad(x2, y2, x1, y1), Mathf.dst(x2, y2, x1, y1));
                vertices[0] = x1 - cx;
                vertices[1] = y1 - cy;
                vertices[2] = col1;
                vertices[3] = u;
                vertices[4] = mv1;
                vertices[5] = mix1;
                CShaders.slash.attribute(0f, in1, index);

                vertices[6] = x1 + cx;
                vertices[7] = y1 + cy;
                vertices[8] = col1;
                vertices[9] = u2;
                vertices[10] = mv1;
                vertices[11] = mix1;
                CShaders.slash.attribute(1f, in1, index);

                vertices[12] = x2 + nx;
                vertices[13] = y2 + ny;
                vertices[14] = col2;
                vertices[15] = u2;
                vertices[16] = mv2;
                vertices[17] = mix2;
                CShaders.slash.attribute(1f, in2, index + 1);

                vertices[18] = x2 - nx;
                vertices[19] = y2 - ny;
                vertices[20] = col2;
                vertices[21] = u;
                vertices[22] = mv2;
                vertices[23] = mix2;
                CShaders.slash.attribute(0f, in2, index + 1);

                Draw.vert(region.texture, vertices, 0, 24);
                lastAngle = z2;
            }

            CShaders.slash.end();
            Draw.shader();

            CShaders.subBuffer.end();
            CShaders.subBuffer.blit(Shaders.screenspace);
        });
    }

    @Override
    public void shorten(){
        if((counter += Time.delta) >= 1f){
            if(points.size >= 5) points.removeRange(0, 4);
            counter %= 1f;
        }

        calcProgress();
    }

    @Override
    public void update(float x, float y, float width){
        if((counter += Time.delta) >= 1f){
            if(points.size > length * 5) points.removeRange(0, 4);

            counter %= 1f;
            points.add(x, y, width, 0f);
            points.add(Mathf.dst(x, y, lastX, lastY) * slashScale);
        }

        lastAngle = -Angles.angleRad(x, y, lastX, lastY);
        lastIntensity = Mathf.dst(x, y, lastX, lastY) * slashScale;
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
            for(int i = 0; i < psize; i += 5){
                float
                    x1 = items[i], y1 = items[i + 1],
                    dst = i < psize - 5 ? Mathf.dst(x1, y1, items[i + 5], items[i + 6]) : Mathf.dst(x1, y1, lastX, lastY);

                maxDist += dst;
                items[i + 3] = dst;
            }

            float frac = length / (points.size / 5f);
            float first = items[3];

            float last = 0f;
            for(int i = 0; i < psize; i += 5){
                float v = items[i + 3];

                float f = items[i + 3] = (v + last - first) / maxDist * frac;
                last += v;

                items[i + 4] *= Interp.pow10Out.apply(f);
            }
        }
    }
}
