package confictura.util;

import arc.graphics.*;
import arc.math.*;

/**
 * Math utilities, providing static useful functions that Arc doesn't provide.
 * @author GlennFolker
 */
public final class MathUtils{
    public static final Color packedMax = pack(new Color(), Float.MAX_VALUE);

    private MathUtils(){
        throw new AssertionError();
    }

    public static Color pack(Color out, float value){
        float exp = Mathf.floorPositive(Mathf.log2(Math.abs(value)) + 1f);
        value /= Mathf.pow(2f, exp);
        value = (value + 1f) * (256f * 256f * 256f - 1f) / (2f * 256f * 256f * 256f);

        float x = value % 1f;
        float y = (value * 256f) % 1f;
        float z = (value * 256f * 256f) % 1f;
        float w = (value * 256f * 256f * 256f) % 1f;

        out.r = x - y / 256f + 1f / 512f;
        out.g = y - z / 256f + 1f / 512f;
        out.b = z - w / 256f + 1f / 512f;
        out.a = (exp + 127.5f) / 256f;
        return out;
    }
}
