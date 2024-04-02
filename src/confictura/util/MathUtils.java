package confictura.util;

import arc.math.*;
import arc.math.geom.*;

/**
 * Provides several convenience mathematical operations, mainly bridging Arc's core module and its 3D extension.
 * @author GlennFolker
 */
public final class MathUtils{
    private MathUtils(){
        throw new AssertionError();
    }

    public static float bound(float in, float from, float to, float start, float end, Interp interpolation){
        if(in < from || in > to){
            return 0f;
        }else{
            float f = (in - from) / (to - from);
            return interpolation.apply(start, end, f);
        }
    }

    public static float curve(float in, float from, float to, float start, float end, Interp interpolation){
        if(in < from){
            return start;
        }else if(in > to){
            return end;
        }else{
            float f = (in - from) / (to - from);
            return interpolation.apply(start, end, f);
        }
    }

    /**
     * Sets a 3x3 matrix to the top left 3x3 corner of the provided 4x4 matrix.
     * @param from The 4x4 matrix. This matrix will not be modified.
     * @param to   The output 3x3 matrix.
     * @return     The output 3x3 matrix, for chaining operations.
     */
    public static Mat copyMatrix(Mat3D from, Mat to){
        float[] in = from.val, out = to.val;
        System.arraycopy(in, Mat3D.M00, out, Mat.M00, 3);
        System.arraycopy(in, Mat3D.M01, out, Mat.M01, 3);
        System.arraycopy(in, Mat3D.M02, out, Mat.M02, 3);
        return to;
    }

    public static Vec3 mul(Mat mat, float x, float y, float z, Vec3 out){
        var m = mat.val;
        return out.set(
            m[Mat.M00] * x + m[Mat.M10] * y + m[Mat.M20] * z,
            m[Mat.M01] * x + m[Mat.M11] * y + m[Mat.M21] * z,
            m[Mat.M02] * x + m[Mat.M12] * y + m[Mat.M22] * z
        );
    }

    public static Vec3 normal(Vec3 out, Vec3 a, Vec3 b, Vec3 c){
        return out.set(b).sub(a).crs(c.x - a.x, c.y - a.y, c.z - a.z).nor();
    }

    public static float absin(float rad){
        return absin(rad, 1f, 1f);
    }

    public static float absin(float rad, float scl, float mag){
        return (Mathf.sin(rad / scl) + 1f) / 2f * mag;
    }
}
