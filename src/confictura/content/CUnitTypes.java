package confictura.content;

import confictura.*;
import confictura.entities.units.*;
import confictura.entities.units.skill.*;
import confictura.gen.*;
import mindustry.type.*;

import static confictura.graphics.CPal.*;

/**
 * Defines the {@linkplain UnitType units} this mod offers.
 * @author GlFolker
 */
public final class CUnitTypes{
    public static CUnitType

    parrier;

    private CUnitTypes(){
        throw new AssertionError();
    }

    /** Instantiates all contents. Called in the main thread in {@link ConficturaMod#loadContent()}. */
    public static void load(){
        parrier = new CUnitType("parrier", SkillMechUnit.class){{
            speed = 0.8f;
            rotateSpeed = 2.4f;
            baseRotateSpeed = 3.6f;
            mechStride = 6f;
            hitSize = 35f / 4f;

            canBoost = true;
            outlineColor = monolithOutline;
            mechLegColor = monolithOutlineLight;

            skills.add(new ParrySkill(name + "-parry"){{

            }});

            weapons.add(new Weapon(name + "-weapon"){{
                top = false;
                x = 39f / 4f;
                y = 17f / 4f;
                recoil = 6f / 4f;
            }});
        }};
    }
}
