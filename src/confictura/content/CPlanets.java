package confictura.content;

import confictura.universe.*;
import mindustry.content.*;
import mindustry.mod.*;
import mindustry.type.*;

/**
 * Defines all the planets this mod provides.
 * @author GlennFolker
 */
public final class CPlanets{
    public static Planet confictura;
    
    private CPlanets(){
        throw new AssertionError();
    }

    /** Initializes this class' contents. Should be called in {@link Mod#loadContent()}. */
    public static void load(){
        /*confictura = new Planet("confictura", Planets.sun, 1f, 3){{
            bloom = true;
            generator = new ConficturaGen();
        }};*/
    }
}
