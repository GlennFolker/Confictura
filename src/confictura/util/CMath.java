package confictura.util;

/** Math utilities class. */
public final class CMath{
    private CMath(){
        throw new AssertionError();
    }

    /** @author EyeOfDarkness */
    public static float angleDistSigned(float a, float b){
        a = (a + 360f) % 360f;
        b = (b + 360f) % 360f;

        float d = Math.abs(a - b) % 360f;
        int sign = (a - b >= 0f && a - b <= 180f) || (a - b <= -180f && a - b >= -360f) ? 1 : -1;

        return (d > 180f ? 360f - d : d) * sign;
    }
}
