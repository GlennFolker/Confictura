package confictura.content;

import arc.graphics.*;
import confictura.entities.bullets.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.type.*;

/**
 * Defines all the planets this mod provides.
 * @author GlennFolker
 */
public final class CUnitTypes{
    public static UnitType
    saber;

    private CUnitTypes(){
        throw new AssertionError();
    }

    /** Initializes this class' contents. Should be called in {@link Mod#loadContent()}. */
    public static void load(){
        EntityMapping.register("confictura-saber", UnitEntity::create);
        saber = new UnitType("saber"){{
            health = 300f;
            speed = 7f;
            accel = 0.16f;
            drag = 0.09f;
            rotateSpeed = 12f;
            flying = true;
            lowAltitude = true;

            weapons.add(new Weapon(){{
                mirror = false;
                x = y = 0f;
                shootSound = Sounds.tractorbeam;
                continuous = true;
                reload = 30f;

                bullet = new BladeBulletType(360f){{
                    length = 240f;
                    lifetime = 320f;
                    drawSize = 560f;

                    trailColor = Color.scarlet.cpy().lerp(Color.white, 0.25f);
                    colors = new Color[]{
                        Color.valueOf("ff344855"),
                        Color.valueOf("ff5448aa"),
                        Color.valueOf("ff7c2a"),
                        Color.white
                    };
                }};
            }});
        }};
    }
}
