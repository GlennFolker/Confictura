package confictura.util;

import arc.util.*;

import static mindustry.Vars.*;

/**
 * Reflection utilities, mainly for wrapping reflective operations to eradicate checked exceptions.
 * @author GlennFolker
 */
@SuppressWarnings("unchecked")
public final class ReflectUtils{
    private ReflectUtils(){
        throw new AssertionError();
    }

    public static <T> @Nullable Class<T> findClass(String name){
        try{
            return (Class<T>)Class.forName(name, true, mods.mainLoader());
        }catch(ClassNotFoundException | NoClassDefFoundError e){
            return null;
        }
    }
}
