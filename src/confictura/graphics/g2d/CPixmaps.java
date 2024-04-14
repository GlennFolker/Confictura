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

    public static void drawCenter(Pixmap dst, Pixmap src){
        dst.draw(src, dst.width / 2 - src.width / 2, dst.height / 2 - src.height / 2, true);
    }

    public static Pixmap copyScaled(Pixmap src, int w, int h){
        var out = new Pixmap(w, h);

        Color a = new Color(), b = new Color(), c = new Color(), d = new Color();
        out.each((x, y) -> {
            float fracX = x / (float)w * src.width, fracY = y / (float)h * src.height;
            int fx = (int)fracX, tx = Math.min(fx + 1, src.width - 1),
                fy = (int)fracY, ty = Math.min(fy + 1, src.height - 1);
            fracX -= fx;
            fracY -= fy;

            a.set(src.getRaw(fx, fy)).lerp(b.set(src.getRaw(tx, fy)), fracX);
            c.set(src.getRaw(fx, ty)).lerp(d.set(src.getRaw(tx, ty)), fracX);
            out.setRaw(x, y, a.lerp(c, fracY).rgba());
        });

        return out;
    }
}
