package confictura.ui;

import arc.graphics.*;
import arc.scene.style.*;
import arc.scene.ui.TextField.*;
import confictura.ui.elements.ScriptArea.*;
import mindustry.gen.*;

public final class CStyles{
    public static TextFieldStyle scriptArea;

    private CStyles(){
        throw new AssertionError();
    }

    public static void load(){
        scriptArea = new ScriptAreaStyle(){{
            font = CFonts.script;
            fontColor = Color.white;
            selection = Tex.selection;
            cursor = Tex.cursor;
            warn = ((TextureRegionDrawable)selection).tint(new Color(Color.yellow).a(0.5f));
            error = ((TextureRegionDrawable)selection).tint(new Color(Color.red).a(0.5f));
        }};
    }
}
