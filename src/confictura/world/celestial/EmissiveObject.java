package confictura.world.celestial;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g3d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import confictura.graphics.*;
import confictura.graphics.shaders.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;

import java.util.*;

import static mindustry.Vars.*;

/**
 * Defines a celestial object that may draw emissive vertices.
 * @author GlFolker
 */
public abstract class EmissiveObject extends Planet{
    public EmissiveBatchShader batchShader = CShaders.emissiveBatch;

    public @Nullable Texture emissiveTexture;
    public @Nullable TextureRegion[] emissiveRegions;
    public Color[] emissions = new Color[0];

    protected @Nullable VertexBatch3D batch;
    protected @Nullable PlanetParams lastParams;
    protected boolean wasAtmospheric;

    public EmissiveObject(String name, Planet parent, float radius){
        super(name, parent, radius);
    }

    @Override
    public void load(){
        super.load();
        if(!headless){
            if(batch == null) batch = new VertexBatch3D(4096, true, true, 1, batchShader);
            if(emissiveTexture == null){
                int columns = Mathf.round(Mathf.sqrt(emissions.length)),
                    rows = columns + Math.max(emissions.length - columns * columns, 0);

                var emission = new Pixmap(columns * 8, rows * 8);
                emission.pixels.limit(emission.pixels.capacity());

                int[] data = new int[8];
                for(int i = 0; i < emissions.length; i++){
                    Arrays.fill(data, emissions[i].rgba());
                    int x = (i % columns) * 8,
                        y = (i / columns) * 8;

                    for(int ty = 0; ty < 8; ty++){
                        emission.pixels.position((x + (y + ty) * emission.width) * 4);
                        emission.pixels.asIntBuffer().put(data, 0, 8);
                    }
                }

                emission.pixels.position(0);
                emissiveTexture = new Texture(emission);
                emissiveRegions = new TextureRegion[emissions.length];

                for(int i = 0; i < emissions.length; i++){
                    int x = i % columns,
                        y = i / columns;

                    float
                        u = (x * 8f + 4f) / emission.width,
                        v = (y * 8f + 4f) / emission.height;

                    var region = emissiveRegions[i] = new TextureRegion();
                    region.texture = emissiveTexture;
                    region.width = region.height = 4;
                    region.u = region.u2 = u;
                    region.v = region.v2 = v;
                }
            }
        }
    }

    @Override
    public void draw(PlanetParams params, Mat3D projection, Mat3D transform){
        // Force draw atmosphere, and emissive vertices along with it.
        // If this becomes an issue, the way to fix it is to purchase a better device.
        wasAtmospheric = params.alwaysDrawAtmosphere;
        params.alwaysDrawAtmosphere = true;
        lastParams = params;

        super.draw(params, projection, transform);
    }

    @Override
    public final void drawAtmosphere(Mesh atmosphere, Camera3D cam){
        Gl.depthMask(false);
        Blending.additive.apply();

        batch.proj(cam.combined);
        batchShader.planet = this;

        drawEmissive();
        batch.flush(Gl.triangles);

        drawActualAtmosphere(atmosphere, cam);

        Blending.normal.apply();
        Gl.depthMask(true);
    }

    public abstract void drawEmissive();

    public void drawActualAtmosphere(Mesh atmosphere, Camera3D cam){
        var shader = Shaders.atmosphere;
        shader.camera = cam;
        shader.planet = this;
        shader.bind();
        shader.apply();
        atmosphere.render(shader, Gl.triangles);
    }
}
