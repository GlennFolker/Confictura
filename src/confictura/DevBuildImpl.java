package confictura;

import arc.util.*;
import arc.util.Log.*;
import confictura.util.*;
import mindustry.ui.dialogs.*;

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
    }
}
