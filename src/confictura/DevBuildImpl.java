package confictura;

import arc.util.*;
import arc.util.Log.*;
import confictura.util.*;
import mindustry.ctype.*;
import mindustry.ui.dialogs.*;

import static arc.Core.*;
import static confictura.ConficturaMod.*;
import static mindustry.Vars.*;

/**
 * Implementation class for {@link DevBuild}. This class is excluded in the built JARs for non-developer builds.
 * @author GlennFolker
 */
public class DevBuildImpl implements DevBuild{
    public DevBuildImpl(){
        Log.level = LogLevel.debug;
        PlanetDialog.debugSelect = true;
    }

    @Override
    public void init(){
        ScriptUtils.importDefaults(ScriptUtils.vanillaScope);
        for(var type : ContentType.all) content.getBy(type).each(c -> c instanceof MappableContent && isConfictura(c), (MappableContent c) -> {
            if(!bundle.has(type.name() + "." + c.name + ".name")){
                Log.debug("Content '@' of type '@' has no localized name.", c.name, type.name());
            }
            if(c instanceof UnlockableContent u && !u.isHidden() && !bundle.has(type.name() + "." + c.name + ".description")){
                Log.debug("Content '@' of type '@' has no localized description.", c.name, type.name());
            }
        });
    }
}
