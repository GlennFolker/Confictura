package confictura.graphics.g2d;

import arc.graphics.g2d.*;

import static arc.Core.*;

public final class Draw2DUtils{
    private Draw2DUtils(){
        throw new AssertionError();
    }

    public static void crectUi(float x, float y, float w, float h){
        Draw.rect(atlas.find("whiteui"), x + w / 2f, y + h / 2f, w, h);
    }
}
