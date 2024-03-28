package confictura.world.celestial;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;

import java.util.*;

// TODO Should probably make this an `abstract class` that extends `Planet` instead.
public interface Emissive{
    Texture getEmissive();

    default void createEmissions(Color[] emissions, Cons<Texture> emissiveTexture, Cons<TextureRegion[]> emissiveRegions){
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
        var outTexture = new Texture(emission);
        var outRegions = new TextureRegion[emissions.length];

        for(int i = 0; i < emissions.length; i++){
            int x = i % columns,
                y = i / columns;

            float
                u = (x * 8f + 4f) / emission.width,
                v = (y * 8f + 4f) / emission.height;

            var region = outRegions[i] = new TextureRegion();
            region.texture = outTexture;
            region.width = region.height = 4;
            region.u = region.u2 = u;
            region.v = region.v2 = v;
        }

        emissiveTexture.get(outTexture);
        emissiveRegions.get(outRegions);
    }
}
