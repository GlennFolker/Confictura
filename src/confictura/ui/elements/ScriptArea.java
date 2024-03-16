package confictura.ui.elements;

import arc.math.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import confictura.ui.*;

import static arc.Core.*;

public class ScriptArea extends TextArea{
    public ScriptArea(String initial){
        super(initial, CStyles.scriptArea);
        setFocusTraversal(false);

        typed(in -> {
            int oldCursor = cursor;
            switch(in){
                case TAB -> {
                    if(hasSelection) break;

                    boolean decrease = input.shift();
                    int line = cursorLine * 2 >= linesBreak.size ? text.length() : linesBreak.items[cursorLine * 2];
                    if(decrease){
                        int indent = indentCount(cursorLine);
                        if(indent > 0){
                            int sub = indent - Mathf.floor(indent / 4f) * 4;
                            if(sub == 0) sub = 4;

                            setText(text.substring(0, line) + text.substring(line + sub));
                            cursor = oldCursor - sub;
                        }
                    }else{
                        int add = 4 - (cursor - line) % 4;

                        setText(text.substring(0, cursor) + new String(new char[add]).replace('\0', ' ') + text.substring(cursor));
                        cursor = oldCursor + add;
                    }
                }
                case '\n', '\r' -> {
                    int count = indentCount(cursorLine);
                    setText(text.substring(0, cursor) + new String(new char[count]).replace('\0', ' ') + text.substring(cursor));
                    cursor = oldCursor + count;
                }
                case '(', '[', '{' -> {
                    setText(text.substring(0, cursor) + switch(in){
                        case '(' -> ')';
                        case '[' -> ']';
                        case '{' -> '}';
                        default -> throw new AssertionError();
                    } + text.substring(cursor));
                    cursor = oldCursor;
                }
                case '"', '\'', '`' -> {
                    int line = cursorLine * 2;
                    if(
                        text.length() <= 1 ||
                        ((cursor == 1 || cursor == linesBreak.items[line] + 1) || !Character.isLetterOrDigit(text.charAt(cursor - 2))) &&
                        ((cursor == text.length() || cursor == linesBreak.items[line + 1]) || !Character.isLetterOrDigit(text.charAt(cursor)))
                    ){
                        setText(text.substring(0, cursor) + in + text.substring(cursor));
                        cursor = oldCursor;
                    }
                }
            }
        });
    }

    protected int indentCount(int line){
        line = Math.max(line * 2, 0);
        if(line >= linesBreak.size) return 0;

        int start = linesBreak.items[line], end = linesBreak.items[line + 1], i = start;
        while(i < linesBreak.items[line + 1] && text.charAt(i) == ' ') ++i;

        return i - start;
    }

    @Override
    protected ScriptAreaListener createInputListener(){
        return new ScriptAreaListener();
    }

    public class ScriptAreaListener extends TextAreaListener{
        @Override
        public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
            moveCursorLine(Mathf.round(amountY));
            return false;
        }

        @Override
        protected void goHome(boolean jump){
            if(jump){
                cursor = 0;
            }else if(cursorLine * 2 < linesBreak.size){
                int begin = linesBreak.items[cursorLine * 2];
                int start = begin + indentCount(cursorLine);
                cursor = cursor == begin || cursor > start ? start : begin;
            }
        }
    }
}
