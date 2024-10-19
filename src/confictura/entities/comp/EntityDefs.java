package confictura.entities.comp;

import confictura.gen.*;
import ent.anno.Annotations.*;
import mindustry.gen.*;

@SuppressWarnings("unused")
class EntityDefs<E>{
    @EntityDef({Unitc.class, Mechc.class, Skillc.class}) E skillMechUnit;
}
