package confictura.universe;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.noise.*;
import confictura.content.*;
import confictura.world.*;
import mindustry.content.*;
import mindustry.maps.generators.*;
import mindustry.world.*;

/**
 * A planet generator revolving around biomes, reserved for {@link CPlanets#confictura}.
 * @author GlennFolker
 */
public class ConficturaGen extends PlanetGenerator{
    private final Color col1 = new Color(), col2 = new Color();
    private final TerrainState terrain = new TerrainState();
    private final Biome[] biomes = {
        CTerrains.rockFallback
    };
    private final Seq<Biome> biomeProgress = new Seq<>(biomes.length);

    protected float terrainNoise(Vec3 position){
        return Ridged.noise3d(0, position.x, position.y, position.z, 2, 0.67f);
    }

    protected TerrainState getState(Vec3 position){
        terrain.temperature = Simplex.noise3d(
            1,
            4d, 0.5d, 0.37d,
            position.x + 13.17d, position.y + 19.23d, position.z + 29.31d
        );

        terrain.precipitation = Mathf.lerp(Simplex.noise3d(
            2,
            7d, 0.32d, 0.48d,
            position.x + 37.41d, position.y + 43.47d, position.z + 49.53d
        ), Ridged.noise3d(
            3,
            position.x + 57.61d, position.y + 67.71d, position.z + 73.79d,
            4, 0.5f
        ), 0.33f);

        terrain.height = Mathf.lerp(Simplex.noise3d(
            4,
            5d, 0.67d, 0.3d,
            position.x + 81.83d, position.y + 87.89d, position.z + 91.97d
        ), Ridged.noise3d(
            5,
            position.x + 101.13d, position.y + 103.17d, position.z + 107.19d,
            7, 0.67f
        ), 0.5f);

        return terrain;
    }

    protected Seq<Biome> getBiomes(Vec3 position){
        biomeProgress.size = 0;
        float totalIntensity = 0f;

        TerrainState state = getState(position);
        for(int i = biomes.length - 1; i >= 0; i--){
            Biome biome = biomes[i];
            if(biome.valid(state)){
                float intensity = biome.getIntensity(state); //TODO noise
                totalIntensity += intensity;

                biomeProgress.add(biome);
                if(totalIntensity > 1f) break;
            }
        }

        return biomeProgress;
    }

    @Override
    protected void genTile(Vec3 position, TileGen tile){
        long seed = position.hashCode();

        TerrainState state = getState(position);
        for(Biome biome : getBiomes(position)){
            rand.setSeed(seed);
            biome.generate(rand, position, state, tile);
        }

        rand.setSeed(seed);
        if(Ridged.noise3d(rand.nextInt(), position.x, position.y, position.z, 2, 22f) > 0.31f){
            tile.block = Blocks.air;
        }
    }

    @Override
    public float getHeight(Vec3 position){
        return getState(position).height;
    }

    @Override
    public Color getColor(Vec3 position){
        long seed = position.hashCode();

        col1.set(0f, 0f, 0f, 1f);
        TerrainState state = getState(position);

        float scl = 0f, persist = 1f;
        for(Biome biome : getBiomes(position)){
            rand.setSeed(seed);

            float mul = biome.getIntensity(state) * persist;
            persist *= 0.3f;
            scl += mul;

            Color map = col2.set(biome.getFloor(rand, position, state).mapColor).mul(mul);
            col1.r += map.r;
            col1.g += map.g;
            col1.b += map.b;
        }

        col1.r /= scl;
        col1.g /= scl;
        col1.b /= scl;
        return col1;
    }
}
