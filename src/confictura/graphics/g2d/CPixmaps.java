package confictura.graphics.g2d;

import arc.graphics.*;
import arc.math.*;

import static arc.graphics.Pixmaps.*;

public final class CPixmaps{
    private CPixmaps(){
        throw new AssertionError();
    }

    public static Pixmap gaussianBlur(Pixmap in, int blurWidth, int blurHeight, float deviation){
        var blur = new float[(2 * blurWidth + 1) * (2 * blurHeight + 1)];
        float s = 2f * deviation * deviation, nor = 0f;

        for(int tx = -blurWidth; tx <= blurWidth; tx++){
            for(int ty = -blurHeight; ty <= blurHeight; ty++){
                float r = Mathf.sqrt(tx * tx + ty * ty);
                float res = Mathf.pow(Mathf.E, -(r * r) / s) / (Mathf.pi * s);

                blur[tx + blurWidth + (ty + blurHeight) * (2 * blurWidth + 1)] = res;
                nor += res;
            }
        }

        for(int i = 0; i < blur.length; i++) blur[i] /= nor;

        var ref = new Pixmap(in.width + blurWidth * 2, in.height + blurHeight * 2);
        ref.draw(in, blurWidth, blurHeight);
        bleed(ref, Integer.MAX_VALUE);

        Color sum = new Color(), col = new Color();

        var out = new Pixmap(ref.width, ref.height);
        ref.each((x, y) -> {
            sum.set(0f, 0f, 0f, 0f);
            for(int tx = -blurWidth; tx <= blurWidth; tx++){
                for(int ty = -blurHeight; ty <= blurHeight; ty++){
                    float factor = blur[tx + blurWidth + (ty + blurHeight) * (2 * blurWidth + 1)];
                    col.set(ref.get(x + tx, y + ty));

                    sum.r += col.r * factor;
                    sum.g += col.g * factor;
                    sum.b += col.b * factor;
                    sum.a += col.a * factor;
                }
            }

            out.setRaw(x, y, sum.rgba());
        });

        ref.dispose();
        return out;
    }
}
