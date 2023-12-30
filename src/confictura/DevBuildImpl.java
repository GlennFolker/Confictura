package confictura;

import arc.util.*;
import arc.util.Log.*;
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
}
