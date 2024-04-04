package confictura.graphics.g2d;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;

import static confictura.util.StructUtils.*;

public abstract class BaseTrail extends Trail{
    public final TrailAttrib[] attributes;
    public final int stride;
    public TrailRotation rot;

    public boolean forceCap;
    public float baseWidth = 1f;
    public Blending blend = Blending.normal;

    public BaseTrail(int length, TrailAttrib... attributes){
        this(length, BaseTrail::defRotation, attributes);
    }

    public BaseTrail(int length, TrailRotation rot, TrailAttrib... attributes){
        super(length);
        this.attributes = attributes;
        this.rot = rot;

        stride = baseStride() + sumi(attributes, t -> t.count);
        points.items = new float[length * stride];

        lastX = lastY = lastW = lastAngle = Float.NaN;
    }

    public int baseStride(){
        // x, y, width, angle, progress.
        return 2 + 1 + 1 + 1;
    }

    @Override
    public BaseTrail copy(){
        var out = copyBlank();
        copyAssign(out);

        return out;
    }

    protected abstract BaseTrail copyBlank();

    protected void copyAssign(BaseTrail out){
        // `Trail` fields.
        out.points.addAll(points);
        out.lastX = lastX;
        out.lastY = lastY;
        out.lastW = lastW;
        out.lastAngle = lastAngle;
        out.counter = counter;

        // `CTrail` fields.
        out.forceCap = forceCap;
        out.baseWidth = baseWidth;
        out.blend = blend;
    }

    @Override
    public int size(){
        return points.size / stride;
    }

    @Override
    public void draw(Color color, float width){
        int len = points.size;
        if(len == 0) return;

        width *= baseWidth;

        Draw.blend(blend);
        if(forceCap) forceDrawCap(color, width);

        var items = points.items;
        for(int i = 0, stride = this.stride; i < len; i += stride) drawSegment(color, width, items, len, i);
        Draw.blend();
    }

    @Override
    public void drawCap(Color color, float width){
        if(!forceCap && points.size > 0){
            Draw.blend(blend);
            forceDrawCap(color, width * baseWidth);
            Draw.blend();
        }
    }

    protected abstract void drawSegment(Color color, float width, float[] points, int len, int offset);

    protected abstract void forceDrawCap(Color color, float width);

    @Override
    public void shorten(){
        int count = (int)(counter += Time.delta), stride = this.stride;
        counter -= count;

        if(count > 0 && points.size >= stride) points.removeRange(0, Math.min(count * stride, points.size) - 1);

        var items = points.items;
        for(int i = 0, len = points.size; i < len; i += stride){
            int offset = i;
            eachAttrib((attrib, off) -> attrib.update(this, items, offset, off));
        }

        calculateProgress();
    }

    @Override
    public void update(float x, float y, float width){
        if(Float.isNaN(lastX)) lastX = x;
        if(Float.isNaN(lastY)) lastY = y;
        if(Float.isNaN(lastW)) lastW = width;

        int stride = this.stride;
        var items = points.items;

        // May be NaN if this is the first `update(x, y, width)`, since there's only one point. That's okay though, since
        // in most cases nothing can be meaningfully drawn anyway. In case where it changed from NaN to valid, update all
        // vertices with invalid angles.
        float angle = rot.get(this, lastX, lastY, lastAngle, x, y);
        if(!Float.isNaN(angle) && Float.isNaN(lastAngle)){
            lastAngle = angle;
            for(int i = 0, len = points.size; i < len && Float.isNaN(angle(items, i)); i += stride){
                angle(items, i, angle);
            }
        }

        int count = (int)(counter += Time.delta);
        counter -= count;

        if(count > 0){
            int added = count * stride;
            if(points.size > length * stride - added) points.removeRange(0, added - 1);

            int len = points.size;
            if(count > 1){
                for(int i = 0; i < count; i++){
                    float f = i / (count - 1f);

                    point(Mathf.lerp(lastX, x, f), Mathf.lerp(lastY, y, f), Mathf.lerp(lastW, width, f), Mathf.slerpRad(lastAngle, angle, f), items, len);
                    len += stride;
                }
            }else{
                point(x, y, width, angle, items, len);
                len += stride;
            }

            points.size = len;
        }

        for(int i = 0, len = points.size; i < len; i += stride){
            int offset = i;
            eachAttrib((attrib, off) -> attrib.update(this, items, offset, off));
        }

        eachAttrib((attrib, off) -> attrib.postUpdate(this, off));

        lastX = x;
        lastY = y;
        lastW = width;
        lastAngle = angle;
        calculateProgress();
    }

    public void calculateProgress(){
        int len = points.size;
        if(len == 0) return;

        int stride = this.stride;
        var items = points.items;

        float max = 0f;
        for(int i = 0; i < len; i += stride){
            float x1 = x(items, i), y1 = y(items, i), x2, y2;
            if(i < len - stride){
                x2 = x(items, i + stride);
                y2 = y(items, i + stride);
            }else{
                x2 = lastX;
                y2 = lastY;
            }

            //TODO Should probably interpolate too if it's being shortened.
            if(i == 0 && len == length * stride){
                x1 = Mathf.lerp(x1, x2, counter);
                y1 = Mathf.lerp(y1, y2, counter);
            }

            float dst = Mathf.dst(x1, y1, x2, y2);

            progress(items, i, max);
            max += dst;
        }

        float frac = (float)len / stride / length;
        for(int i = 0; i < len; i += stride){
            progress(items, i, (progress(items, i) / max) * frac);
        }
    }

    public void eachAttrib(AttribCons cons){
        for(int i = 0, off = baseStride(); i < attributes.length; i++){
            var attrib = attributes[i];
            cons.get(attrib, off);

            off += attrib.count;
        }
    }

    public void point(float x, float y, float width, float angle, float[] points, int offset){
        basePoint(x, y, width, angle, points, offset);
        eachAttrib((attrib, off) -> attrib.point(this, x, y, width, angle, points, offset, off));
    }

    public void basePoint(float x, float y, float width, float angle, float[] points, int offset){
        points[offset] = x;
        points[offset + 1] = y;
        points[offset + 2] = width;
        points[offset + 3] = angle;
        points[offset + 4] = 0f;
    }

    public float x(float[] points, int offset){
        return points[offset];
    }

    public void x(float[] points, int offset, float x){
        points[offset] = x;
    }

    public float y(float[] points, int offset){
        return points[offset + 1];
    }

    public void y(float[] points, int offset, float y){
        points[offset + 1] = y;
    }

    public float width(float[] points, int offset){
        return points[offset + 2];
    }

    public void width(float[] points, int offset, float width){
        points[offset + 2] = width;
    }

    public float angle(float[] points, int offset){
        return points[offset + 3];
    }

    public void angle(float[] points, int offset, float angle){
        points[offset + 3] = angle;
    }

    public float progress(float[] points, int offset){
        return points[offset + 4];
    }

    public void progress(float[] points, int offset, float progress){
        points[offset + 4] = progress;
    }

    public static float defRotation(BaseTrail trail, float lastX, float lastY, float lastAngle, float x, float y){
        return Mathf.zero(Mathf.dst2(x, y, lastX, lastY), 0.001f) ? lastAngle : -Angles.angleRad(x, y, lastX, lastY);
    }

    public static TrailRotation entRotation(Rotc unit){
        return (t, lastX, lastY, lastAngle, x, y) -> !unit.isAdded() ? defRotation(t, lastX, lastY, lastAngle, x, y) : convRotation(unit.rotation());
    }

    public static float convRotation(float degrees){
        return (degrees - 180f) * -Mathf.degRad;
    }

    public static float unconvRotation(float angle){
        return -Mathf.radDeg * angle + 180f;
    }

    public interface TrailRotation{
        float get(BaseTrail trail, float lastX, float lastY, float lastAngle, float x, float y);
    }

    public interface AttribCons{
        void get(TrailAttrib attrib, int offset);
    }

    public static abstract class TrailAttrib{
        public final int count;

        protected TrailAttrib(int count){
            this.count = count;
        }

        public abstract void point(BaseTrail trail, float x, float y, float width, float angle, float[] points, int baseOffset, int offset);

        public abstract void update(BaseTrail trail, float[] points, int baseOffset, int offset);

        public abstract void postUpdate(BaseTrail trail, int offset);

        public abstract TrailAttrib copy();
    }
}
