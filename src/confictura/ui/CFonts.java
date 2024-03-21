package confictura.ui;

import arc.assets.*;
import arc.files.*;
import arc.freetype.*;
import arc.freetype.FreeTypeFontGenerator.*;
import arc.freetype.FreetypeFontLoader.*;
import arc.graphics.g2d.*;
import arc.struct.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public final class CFonts{
    public static Font script;

    private CFonts(){
        throw new AssertionError();
    }

    public static void load(){
        var fontSuffix = ".confictura.gen";
        assets.setLoader(FreeTypeFontGenerator.class, ".confictura.gen", new FreeTypeFontGeneratorLoader(tree){
            @Override
            public FreeTypeFontGenerator load(AssetManager assetManager, String fileName, Fi file, FreeTypeFontGeneratorParameters parameter){
                return new FreeTypeFontGenerator(resolve(fileName.substring(0, fileName.length() - fontSuffix.length())));
            }
        });

        assets.setLoader(Font.class, "-confictura", new FreetypeFontLoader(tree){
            @Override
            public Font loadSync(AssetManager manager, String fileName, Fi file, FreeTypeFontLoaderParameter parameter){
                if(parameter == null) throw new IllegalArgumentException("FreetypeFontParameter must be set in AssetManager#load to point at a TTF file!");
                return manager
                    .get(parameter.fontFileName + fontSuffix, FreeTypeFontGenerator.class)
                    .generateFont(parameter.fontParameters);
            }

            @Override
            @SuppressWarnings("rawtypes")
            public Seq<AssetDescriptor> getDependencies(String fileName, Fi file, FreeTypeFontLoaderParameter parameter){
                return Seq.with(new AssetDescriptor<>(parameter.fontFileName + fontSuffix, FreeTypeFontGenerator.class));
            }
        });

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
