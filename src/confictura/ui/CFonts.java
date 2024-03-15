package confictura.ui;

import arc.freetype.*;
import arc.freetype.FreeTypeFontGenerator.*;
import arc.freetype.FreetypeFontLoader.*;
import arc.graphics.g2d.*;

import static arc.Core.*;

public final class CFonts{
    public static Font script;

    private CFonts(){
        throw new AssertionError();
    }

    public static void load(){
        assets.load("script-confictura", Font.class, new FreeTypeFontLoaderParameter("fonts/script.ttf", new FreeTypeFontParameter(){{
            size = 20;
            incremental = true;
            renderCount = 1;
            characters = FreeTypeFontGenerator.DEFAULT_CHARS;
        }})).loaded = f -> {
            f.setFixedWidthGlyphs(FreeTypeFontGenerator.DEFAULT_CHARS);
            script = f;
        };
    }
}
