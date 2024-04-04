package confictura.graphics.g2d;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;

import static arc.Core.*;
import static confictura.util.StructUtils.*;

public class TexturedTrail extends BaseTrail{
    public final String name;
    public TextureRegion region, capRegion;

    public float shrink = 1f;
    public float fadeAlpha = 0f;
    public float mixAlpha = 0.5f;
    public Color fadeColor = Color.white;
    public Interp gradientInterp = Interp.linear;
    public Interp fadeInterp = Interp.pow2In;
    public Interp sideFadeInterp = Interp.pow3In;
    public Interp mixInterp = Interp.pow5In;

    private static final float[] vertices = new float[24];
    private static final Color tmp = new Color();

    public TexturedTrail(int length, String name, TrailAttrib... attributes){
        super(length, attributes);
        this.name = name;
    }

    public TexturedTrail(int length, String name, TrailRotation rot, TrailAttrib... attributes){
        super(length, rot, attributes);
        this.name = name;
    }

    @Override
    protected TexturedTrail copyBlank(){
        return new TexturedTrail(length, name, rot, copyArray(attributes, TrailAttrib::copy));
    }

    @Override
    protected void copyAssign(BaseTrail out){
        super.copyAssign(out);
        if(out instanceof TexturedTrail o){
            o.region = region;
            o.capRegion = capRegion;

            o.shrink = shrink;
            o.fadeAlpha = fadeAlpha;
            o.mixAlpha = mixAlpha;
            o.fadeColor = fadeColor;
            o.gradientInterp = gradientInterp;
            o.fadeInterp = fadeInterp;
            o.sideFadeInterp = sideFadeInterp;
            o.mixInterp = mixInterp;
        }
    }

    @Override
    protected void drawSegment(Color color, float width, float[] points, int len, int offset){
        if(region == null) region = atlas.find(name);

        int stride = this.stride;
        float
            u = region.u, v = region.v2, u2 = region.u2, v2 = region.v, uh = Mathf.lerp(u, u2, 0.5f),
            x1 = x(points, offset), y1 = y(points, offset), w1 = width(points, offset), r1 = angle(points, offset), p1 = progress(points, offset),
            x2, y2, w2, r2, p2;

        if(offset < len - stride){
            int next = offset + stride;
            x2 = x(points, next);
            y2 = y(points, next);
            w2 = width(points, next);
            r2 = angle(points, next);
            p2 = progress(points, next);

            //TODO Should probably interpolate too if it's being shortened.
            if(offset == 0 && len == length * stride){
                x1 = Mathf.lerp(x1, x2, counter);
                y1 = Mathf.lerp(y1, y2, counter);
                w1 = Mathf.lerp(w1, w2, counter);
                r1 = Mathf.slerpRad(r1, r2, counter);
                // Don't interpolate `p1` into `p2`, as it is already handled.
            }
        }else{
            x2 = lastX;
            y2 = lastY;
            w2 = lastW;
            r2 = lastAngle;
            p2 = (float)len / stride / length;
        }

        float
            fs1 = Mathf.map(p1, 1f - shrink, 1f) * width * w1,
            fs2 = Mathf.map(p2, 1f - shrink, 1f) * width * w2,

            cx = Mathf.sin(r1) * fs1, cy = Mathf.cos(r1) * fs1,
            nx = Mathf.sin(r2) * fs2, ny = Mathf.cos(r2) * fs2,

            mv1 = Mathf.lerp(v, v2, p1), mv2 = Mathf.lerp(v, v2, p2),
            cv1 = p1 * fadeAlpha + (1f - fadeAlpha), cv2 = p2 * fadeAlpha + (1f - fadeAlpha),
            col1 = tmp.set(Draw.getColor()).lerp(fadeColor, gradientInterp.apply(1f - p1)).a(fadeInterp.apply(cv1)).clamp().toFloatBits(),
            col1h = tmp.set(Draw.getColor()).lerp(fadeColor, gradientInterp.apply(1f - p1)).a(sideFadeInterp.apply(cv1)).clamp().toFloatBits(),
            col2 = tmp.set(Draw.getColor()).lerp(fadeColor, gradientInterp.apply(1f - p2)).a(fadeInterp.apply(cv2)).clamp().toFloatBits(),
            col2h = tmp.set(Draw.getColor()).lerp(fadeColor, gradientInterp.apply(1f - p2)).a(sideFadeInterp.apply(cv2)).clamp().toFloatBits(),
            mix1 = tmp.set(color).a(mixInterp.apply(p1 * mixAlpha)).clamp().toFloatBits(),
            mix2 = tmp.set(color).a(mixInterp.apply(p2 * mixAlpha)).clamp().toFloatBits();

        vertices[0] = x1 - cx;
        vertices[1] = y1 - cy;
        vertices[2] = col1h;
        vertices[3] = u;
        vertices[4] = mv1;
        vertices[5] = mix1;

        vertices[6] = x1;
        vertices[7] = y1;
        vertices[8] = col1;
        vertices[9] = uh;
        vertices[10] = mv1;
        vertices[11] = mix1;

        vertices[12] = x2;
        vertices[13] = y2;
        vertices[14] = col2;
        vertices[15] = uh;
        vertices[16] = mv2;
        vertices[17] = mix2;

        vertices[18] = x2 - nx;
        vertices[19] = y2 - ny;
        vertices[20] = col2h;
        vertices[21] = u;
        vertices[22] = mv2;
        vertices[23] = mix2;

        Draw.vert(region.texture, vertices, 0, 24);

        vertices[0] = x1 + cx;
        vertices[1] = y1 + cy;
        vertices[2] = col1h;
        vertices[3] = u2;
        vertices[4] = mv1;
        vertices[5] = mix1;

        vertices[6] = x1;
        vertices[7] = y1;
        vertices[8] = col1;
        vertices[9] = uh;
        vertices[10] = mv1;
        vertices[11] = mix1;

        vertices[12] = x2;
        vertices[13] = y2;
        vertices[14] = col2;
        vertices[15] = uh;
        vertices[16] = mv2;
        vertices[17] = mix2;

        vertices[18] = x2 + nx;
        vertices[19] = y2 + ny;
        vertices[20] = col2h;
        vertices[21] = u2;
        vertices[22] = mv2;
        vertices[23] = mix2;

        Draw.vert(region.texture, vertices, 0, 24);
    }

    @Override
    protected void forceDrawCap(Color color, float width){
        if(capRegion == null) capRegion = atlas.find(name + "-cap", "confictura-trail-cap");

        int len = points.size;
        float
            rv = (float)len / stride / length,
            alpha = rv * fadeAlpha + (1f - fadeAlpha),
            w = Mathf.map(rv, 1f - shrink, 1f) * width * lastW * 2f,
            h = ((float)capRegion.height / capRegion.width) * w,

            angle = unconvRotation(lastAngle) - 90f,
            u = capRegion.u, v = capRegion.v2, u2 = capRegion.u2, v2 = capRegion.v, uh = Mathf.lerp(u, u2, 0.5f),
            cx = Mathf.cosDeg(angle) * w / 2f, cy = Mathf.sinDeg(angle) * w / 2f,
            x1 = lastX, y1 = lastY,
            x2 = lastX + Mathf.cosDeg(angle + 90f) * h, y2 = lastY + Mathf.sinDeg(angle + 90f) * h,

            col1 = tmp.set(Draw.getColor()).lerp(fadeColor, gradientInterp.apply(1f - rv)).a(fadeInterp.apply(alpha)).clamp().toFloatBits(),
            col1h = tmp.set(Draw.getColor()).lerp(fadeColor, gradientInterp.apply(1f - rv)).a(sideFadeInterp.apply(alpha)).clamp().toFloatBits(),
            col2 = tmp.set(Draw.getColor()).lerp(fadeColor, gradientInterp.apply(1f - rv)).a(fadeInterp.apply(alpha)).clamp().toFloatBits(),
            col2h = tmp.set(Draw.getColor()).lerp(fadeColor, gradientInterp.apply(1f - rv)).a(sideFadeInterp.apply(alpha)).clamp().toFloatBits(),
            mix1 = tmp.set(color).a(mixInterp.apply(rv * mixAlpha)).clamp().toFloatBits(),
            mix2 = tmp.set(color).a(mixInterp.apply(rv * mixAlpha)).clamp().toFloatBits();

        vertices[0] = x1 - cx;
        vertices[1] = y1 - cy;
        vertices[2] = col1h;
        vertices[3] = u;
        vertices[4] = v;
        vertices[5] = mix1;

        vertices[6] = x1;
        vertices[7] = y1;
        vertices[8] = col1;
        vertices[9] = uh;
        vertices[10] = v;
        vertices[11] = mix1;

        vertices[12] = x2;
        vertices[13] = y2;
        vertices[14] = col2;
        vertices[15] = uh;
        vertices[16] = v2;
        vertices[17] = mix2;

        vertices[18] = x2 - cx;
        vertices[19] = y2 - cy;
        vertices[20] = col2h;
        vertices[21] = u;
        vertices[22] = v2;
        vertices[23] = mix2;

        Draw.vert(capRegion.texture, vertices, 0, 24);

        vertices[0] = x1 + cx;
        vertices[1] = y1 + cy;
        vertices[2] = col1h;
        vertices[3] = u2;
        vertices[4] = v;
        vertices[5] = mix1;

        vertices[6] = x1;
        vertices[7] = y1;
        vertices[8] = col1;
        vertices[9] = uh;
        vertices[10] = v;
        vertices[11] = mix1;

        vertices[12] = x2;
        vertices[13] = y2;
        vertices[14] = col2;
        vertices[15] = uh;
        vertices[16] = v2;
        vertices[17] = mix2;

        vertices[18] = x2 + cx;
        vertices[19] = y2 + cy;
        vertices[20] = col2h;
        vertices[21] = u2;
        vertices[22] = v2;
        vertices[23] = mix2;

        Draw.vert(capRegion.texture, vertices, 0, 24);
    }
}
