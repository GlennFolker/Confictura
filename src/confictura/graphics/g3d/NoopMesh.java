package confictura.graphics.g3d;

import arc.math.geom.*;
import mindustry.graphics.g3d.*;

/**
 * A generic mesh that doesn't actually do anything.
 * @author GlennFolker
 */
public class NoopMesh implements GenericMesh{
    @Override
    public void render(PlanetParams params, Mat3D projection, Mat3D transform){}
}
