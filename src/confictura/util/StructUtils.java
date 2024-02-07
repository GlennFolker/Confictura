package confictura.util;

import arc.func.*;

/**
 * Struct utilities, providing some stateless iterative utilities such as reduce.
 * @author GlennFolker
 */
public final class StructUtils{
    private StructUtils(){
        throw new AssertionError();
    }

    public static <T> boolean equals(T[] first, T[] second){
        int len;
        if((len = first.length) != second.length) return false;

        for(int i = 0; i < len; i++){
            if(!first[i].equals(second[i])) return false;
        }
        return true;
    }

    public static <T> int reducei(T[] array, int initial, Reducei<T> reduce){
        for(var item : array) initial = reduce.get(item, initial);
        return initial;
    }

    public static <T> int sumi(T[] array, Intf<T> extract){
        return reducei(array, 0, (item, accum) -> accum + extract.get(item));
    }

    public static <T> float reducef(T[] array, float initial, Reducef<T> reduce){
        for(var item : array) initial = reduce.get(item, initial);
        return initial;
    }

    public static <T> float average(T[] array, Floatf<T> extract){
        return reducef(array, 0f, (item, accum) -> accum + extract.get(item)) / array.length;
    }

    public interface Reducei<T>{
        int get(T item, int accum);
    }

    public interface Reducef<T>{
        float get(T item, float accum);
    }
}
