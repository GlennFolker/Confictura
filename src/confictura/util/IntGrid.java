package confictura.util;

import arc.util.*;

import java.util.*;

public class IntGrid{
    public int[] items;
    public int width, height;

    public IntGrid(int width, int height){
        resize(width, height);
    }

    public boolean in(int x, int y){
        return Structs.inBounds(x, y, width, height);
    }

    public int get(int x, int y){
        if(!in(x, y)) return -1;
        return items[y * width + x];
    }

    public void set(int x, int y, int value){
        if(in(x, y)) items[y * width + x] = value;
    }

    public void each(IntTileCons cons){
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                cons.get(x, y, get(x, y));
            }
        }
    }

    public void fill(int value){
        Arrays.fill(items, value);
    }

    public void resize(int width, int height){
        items = new int[(this.width = width) * (this.height = height)];
    }

    public interface IntTileCons{
        void get(int x, int y, int value);
    }
}
