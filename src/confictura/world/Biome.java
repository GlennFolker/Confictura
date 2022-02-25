package confictura.world;

import arc.math.*;
import arc.math.geom.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;

/**
 * Defines an abstract biome; a block lookup used in generation of 2D tilemaps and planet surfaces.
 * @author GlennFolker
 */
public abstract class Biome{
    /** The minimum temperature of this biome. Ranges in [0..1]. */
    public float minTemperature = 0f;
    /** The maximum temperature of this biome. Ranges in [0..1]. */
    public float maxTemperature = 1f;
    /** The minimum precipitation of this biome. Ranges in [0..1]. */
    public float minPrecipitation = 0f;
    /** The maximum precipitation of this biome. Ranges in [0..1]. */
    public float maxPrecipitation = 1f;
    /** The minimum height of this biome. Ranges in [0..1]. */
    public float minHeight = 0f;
    /** The maximum height of this biome. Ranges in [0..1]. */
    public float maxHeight = 1f;

    /**
     * Looks a floor up of this biome for generation.
     * @param rand     The seeded random.
     * @param position The block position.
     * @param state    The terrain state.
     * @return         The floor to be used in generation.
     */
    public abstract Floor getFloor(Rand rand, Vec3 position, TerrainState state);

    /**
     * Generates a tileset based off of a position and a terrain state.
     * @param rand     The seeded random.
     * @param position The block position.
     * @param state    The terrain state.
     * @param gen      The resulting tile set to be used.
     */
    public abstract void generate(Rand rand, Vec3 position, TerrainState state, TileGen gen);

    /**
     * @param state The terrain state.
     * @return      The temperature of the state, mapped to be in range of the biome thresholds.
     */
    public float mapTemperature(TerrainState state){
        return Mathf.map(state.temperature, minTemperature, maxTemperature);
    }

    /**
     * @param state The terrain state.
     * @return      The precipitation of the state, mapped to be in range of the biome thresholds.
     */
    public float mapPrecipitation(TerrainState state){
        return Mathf.map(state.precipitation, minPrecipitation, maxPrecipitation);
    }

    /**
     * @param state The terrain state.
     * @return      The height of the state, mapped to be in range of the biome thresholds.
     */
    public float mapHeight(TerrainState state){
        return Mathf.map(state.height, minHeight, maxHeight);
    }

    /**
     * @param state The terrain state.
     * @return      How intense should the block generation be.
     */
    public float getIntensity(TerrainState state){
        return mapTemperature(state) * mapPrecipitation(state) * mapHeight(state);
    }

    /**
     * @param state The terrain state.
     * @return      Whether this biome is in a valid terrain state.
     */
    public boolean valid(TerrainState state){
        return
            state.temperature >= minTemperature && state.temperature <= maxTemperature &&
            state.precipitation >= minPrecipitation && state.precipitation <= maxPrecipitation &&
            state.height >= minHeight && state.height <= maxHeight;
    }
}
