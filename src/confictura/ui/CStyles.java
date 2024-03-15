package confictura.ui;

import arc.graphics.*;
import arc.scene.ui.TextField.*;
import mindustry.gen.*;

public final class CStyles{
    public static TextFieldStyle scriptArea;

    private CStyles(){
        throw new AssertionError();
    }

    public static void load(){
        scriptArea = new TextFieldStyle(){{
            font = CFonts.script;
            fontColor = Color.white;
            selection = Tex.selection;
            cursor = Tex.cursor;
        }};
    }
}
