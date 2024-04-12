package confictura.content;

import confictura.*;
import confictura.entities.units.*;
import confictura.entities.units.skill.*;
import confictura.gen.*;
import mindustry.type.*;

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
            canBoost = true;

            skills.add(new ParrySkill(name + "-parry"));
        }};
    }
}
