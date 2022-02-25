package confictura.world;

/**
 * A state of temperature, precipitation, and height. Used for determining {@link Biome}s.
 * @author GlennFolker
 */
public class TerrainState{
    /** The temperature state. Should range in [0..1]. */
    public float temperature;
    /** The precipitation state. Should range in [0..1]. */
    public float precipitation;
    /** The height state. Should range in [0..1]. */
    public float height;

    public TerrainState(){}

    public TerrainState(float temperature, float precipitation, float height){
        this.temperature = temperature;
        this.precipitation = precipitation;
        this.height = height;
    }
}
