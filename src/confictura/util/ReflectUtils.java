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

    /**
     * Finds and casts a class with the specified name using Mindustry's mod class loader.
     * @param name The class' binary name, as per {@link Class#getName()}.
     * @param <T>  The arbitrary type parameter to cast the class into.
     * @return     The casted class, or {@code null} if not found.
     */
    public static <T> @Nullable Class<T> findClass(String name){
        try{
            return (Class<T>)Class.forName(name, true, mods.mainLoader());
        }catch(ClassNotFoundException | NoClassDefFoundError e){
            return null;
        }
    }
}
