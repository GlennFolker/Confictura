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
}
