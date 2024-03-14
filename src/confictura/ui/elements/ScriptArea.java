package confictura.ui.elements;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.struct.*;
import arc.util.*;
import arc.util.pooling.*;
import arc.util.pooling.Pool.*;
import confictura.ui.*;
import confictura.util.*;
import rhino.*;
import rhino.ast.*;

public class ScriptArea extends TextArea{
    protected static final Pool<Diagnostic> diagPool = new Pool<>(){
        @Override
        protected Diagnostic newObject(){
            return new Diagnostic();
        }
    };

    protected static final Pool<Tint> tintPool = new Pool<>(){
        @Override
        protected Tint newObject(){
            return new Tint();
        }
    };

    protected final Seq<Diagnostic> diagnostics = new Seq<>();
    protected final Seq<Tint> tints = new Seq<>();
    protected final IntSeq tintLines = new IntSeq();

    protected final boolean initialized;
    protected FontCache cache;

    public ScriptArea(String text){
        super(text, CStyles.scriptArea);
        setFocusTraversal(false);

        initialized = true;
    }

    @Override
    protected void updateDisplayText(){
        super.updateDisplayText();
        if(!initialized) return;

        var parser = new Parser(new CompilerEnvirons(){{
            setRecoverFromErrors(true);
            setRecordingComments(true);
            setStrictMode(true);
            setWarnTrailingComma(true);
            setLanguageVersion(ScriptUtils.context.getLanguageVersion());
            setReservedKeywordAsIdentifier(false);
            setIdeMode(true);
            setErrorReporter(new ScriptReporter());
        }});
        parser.setDefaultUseStrictDirective(true);

        diagPool.freeAll(diagnostics);
        diagnostics.clear();

        tintPool.freeAll(tints);
        tints.clear();
        tintLines.clear();

        var style = getStyle();
        var def = style.fontColor;
        var keyword = style.keywordColor != null ? style.keywordColor : Tmp.c1.set(Color.royal);

        parser.parse(displayText.toString(), null, 0).visitAll(node -> {
            if(node instanceof Loop loop){
                int pos = node.getAbsolutePosition();

                var key = Token.keywordToName(node.getType());
                tints.add(tint(keyword, pos));
                if(loop.getLp() != -1) tints.add(tint(def, pos + loop.getLp()));
                if(loop.getRp() != -1) tints.add(tint(def, pos + loop.getRp()));

                var body = loop.getBody();
                int bodyPos = body.getAbsolutePosition();
                tints.add(tint(def, bodyPos));
                tints.add(tint(def, bodyPos + body.getLength()));

                if(loop instanceof DoLoop d){
                    if(d.getWhilePosition() != -1) tints.add(tint(keyword, pos + d.getWhilePosition()));
                }
            }

            return true;
        });

        tints.sortComparing(t -> t.offset);
    }

    @Override
    protected void calculateOffsets(){
        super.calculateOffsets();

        tintLines.clear();
        for(int i = 0, j = 0; i < linesBreak.size - 1; i += 2){
            int begin = linesBreak.items[i], end = linesBreak.items[i + 1];

            int from = j;
            while(j < tints.size){
                var tint = tints.get(j);
                if(tint.offset >= begin && tint.offset < end){
                    ++j;
                }else{
                    break;
                }
            }

            tintLines.add(from, j);
        }
    }

    protected Tint tint(Color color, int offset){
        var tint = tintPool.obtain();
        tint.color.set(color);
        tint.offset = offset;
        return tint;
    }

    @Override
    public void setStyle(TextFieldStyle style){
        if(!(style instanceof ScriptAreaStyle s)) throw new IllegalArgumentException("Must be ScriptAreaStyle.");
        super.setStyle(style);

        if(cache != null) cache.clear();
        cache = style.font.newFontCache();
    }

    @Override
    public ScriptAreaStyle getStyle(){
        if(!(style instanceof ScriptAreaStyle s)) throw new IllegalArgumentException("Must be ScriptAreaStyle.");
        return s;
    }

    @Override
    public void draw(){
        super.draw();

        var style = getStyle();
        var font = style.font;
        var background = style.background;

        float bgLeftWidth = 0f;
        if(background != null){
            bgLeftWidth = background.getLeftWidth();
        }

        for(var diag : diagnostics){
            float x = this.x + bgLeftWidth, y = this.y + getTextY(font, background);

            if(!diag.error && style.warn == null || diag.error && style.error == null) continue;
            var drawable = diag.error ? style.error : style.warn;

            float offsetY = 0f;
            int from = diag.offset, to = from + diag.length;
            for(int i = firstLineShowing * 2; i < linesBreak.size - 1 && i < (firstLineShowing + linesShowing) * 2; i += 2){
                int lineStart = linesBreak.get(i), lineEnd = linesBreak.get(i + 1);
                if(!(
                    (from < lineStart && from < lineEnd && to < lineStart && to < lineEnd) ||
                    (from > lineStart && from > lineEnd && to > lineStart && to > lineEnd)
                )){
                    int start = Math.min(Math.max(lineStart, from), glyphPositions.size - 1),
                        end = Math.min(Math.min(lineEnd, to), glyphPositions.size - 1);

                    float offsetX = glyphPositions.get(start) - glyphPositions.get(Math.min(lineStart, glyphPositions.size - 1));
                    float width = start == end ? font.getSpaceXadvance() : (glyphPositions.get(end) - glyphPositions.get(start));

                    drawable.draw(x + offsetX + fontOffset, y - textHeight - font.getDescent() - offsetY, width, font.getLineHeight());
                }

                offsetY += font.getLineHeight();
            }
        }
    }

    @Override
    protected void drawText(Font font, float x, float y){
        var data = font.getData();
        boolean markup = data.markupEnabled;
        data.markupEnabled = false;

        cache.clear();

        float offsetY = 0f;
        for(int i = firstLineShowing * 2; i < (firstLineShowing + linesShowing) * 2 && i < linesBreak.size - 1; i += 2){
            int begin = linesBreak.items[i], end = linesBreak.items[i + 1];

            if(tints.isEmpty()){
                cache.setColor(style.fontColor);
                cache.addText(displayText, x, y + offsetY, begin, end, 0f, Align.left, false);
            }else{
                int from = tintLines.items[i], to = tintLines.items[i + 1];
                if(from == to){
                    cache.addText(displayText, x, y + offsetY, begin, end, 0f, Align.left, false);
                }else{
                    float offsetX = 0f;

                    int start = begin;
                    for(int j = from; j < to; j++){
                        var tint = tints.get(j);

                        var layout = cache.addText(displayText, x + offsetX, y + offsetY, start, tint.offset, 0f, Align.left, false);
                        if(layout.runs.any() && layout.runs.peek().glyphs.any()){
                            var last = layout.runs.peek().glyphs.peek();
                            offsetX += layout.width + last.getKerning(displayText.charAt(tint.offset - 1)) * font.getScaleX();
                        }

                        start = tint.offset;
                        cache.setColor(tint.color);
                    }

                    if(start < end) cache.addText(displayText, x + offsetX, y + offsetY, start, end, 0f, Align.left, false);
                }
            }

            offsetY -= font.getLineHeight();
        }

        cache.draw();
        data.markupEnabled = markup;
    }

    @Override
    protected ScriptAreaListener createInputListener(){
        return new ScriptAreaListener();
    }

    public class ScriptReporter implements IdeErrorReporter{
        @Override
        public void warning(String message, String src, int offset, int length){
            var diag = diagPool.obtain();
            diag.message = message;
            diag.error = false;
            diag.offset = offset;
            diag.length = length;
            diagnostics.add(diag);
        }

        @Override
        public void error(String message, String src, int offset, int length){
            var diag = diagPool.obtain();
            diag.message = message;
            diag.error = true;
            diag.offset = offset;
            diag.length = length;
            diagnostics.add(diag);
        }

        @Override
        public void warning(String message, String src, int line, String lineSrc, int offset){
            throw new UnsupportedOperationException();
        }

        @Override
        public void error(String message, String src, int line, String lineSrc, int offset){
            throw new UnsupportedOperationException();
        }

        @Override
        public EvaluatorException runtimeError(String message, String src, int line, String lineSrc, int offset){
            throw new UnsupportedOperationException();
        }
    }

    public static class Diagnostic implements Poolable{
        public String message;
        public boolean error;
        public int offset, length;

        @Override
        public void reset(){
            message = null;
            error = false;
            offset = length = 0;
        }
    }

    public static class Tint implements Poolable{
        public Color color = new Color();
        public int offset;

        @Override
        public void reset(){
            color.set(Color.white);
            offset = 0;
        }
    }

    public class ScriptAreaListener extends TextAreaListener{
        @Override
        public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
            moveCursorLine(Mathf.round(amountY));
            return false;
        }
    }

    public static class ScriptAreaStyle extends TextFieldStyle{
        public Drawable warn, error;

        public Color keywordColor;
    }
}
