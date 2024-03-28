package confictura.world.celestial;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g3d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import confictura.graphics.*;
import mindustry.game.EventType.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class Satellite extends Planet implements Emissive{
    private static final Quat quat = new Quat();

    public Cons2<Shader, Mat3D> drawStructure = (shader, transform) -> {};
    public Cons<VertexBatch3D> drawEmissive = batch -> {};

    public @Nullable Texture emissiveTexture;
    public @Nullable TextureRegion[] emissiveRegions;
    public Color[] emissions = new Color[0];

    protected @Nullable VertexBatch3D batch;
    protected @Nullable PlanetParams lastParams;
    protected boolean wasAtmospheric;

    public Satellite(String name, Planet observed, float distance){
        // Set parent to `null` temporarily; see below.
        super(name, null, 0.1f);

        updateLighting = drawOrbit = false;
        defaultEnv = Env.space;
        meshLoader = SatelliteMesh::new;
        tidalLock = true;

        // Delay parent setup so it can insert itself as the first child.
        // This *might* be invasive for mods that are sensitive towards their planet children indices (which I can
        // literally find no reason for), however until something weird happens, I will keep this code.
        Events.on(ContentInitEvent.class, e -> {
            parent = observed;
            solarSystem = parent.solarSystem;

            orbitRadius = parent.radius + distance + radius;
            orbitTime = 150f;
            clipRadius = distance + radius + parent.radius * 2f;

            parent.children.insert(0, this);
            float delta = orbitRadius + totalRadius;

            for(int i = 1; i < parent.children.size; i++) parent.children.get(i).orbitRadius += delta;
            parent.updateTotalRadius();
        });
    }

    @Override
    public void load(){
        super.load();
        if(!headless){
            if(batch == null) batch = new VertexBatch3D(4096, true, true, 1, CShaders.emissiveBatch);
            if(emissiveTexture == null) createEmissions(emissions, t -> emissiveTexture = t, r -> emissiveRegions = r);
        }
    }

    @Override
    public Texture getEmissive(){
        return emissiveTexture;
    }

    public float period(float period, Interp interpolation){
        return period(period, 0f, 0f, 1f, interpolation);
    }

    public float period(float period, float offset, float from, float to, Interp interpolation){
        float frac = (Time.globalTime + offset * period) / period;
        return interpolation.apply(Mathf.curve(frac % 1f, from, to));
    }

    public void drawEmissive(){
        CShaders.emissiveBatch.planet = this;

        drawEmissive.get(batch);
        batch.flush(Gl.triangles);
    }

    @Override
    public void draw(PlanetParams params, Mat3D projection, Mat3D transform){
        // Force draw emissions.
        // If this becomes an issue, the way to fix it is to purchase a better device.
        wasAtmospheric = params.alwaysDrawAtmosphere;
        params.alwaysDrawAtmosphere = true;
        lastParams = params;

        super.draw(params, projection, transform);
    }

    @Override
    public void drawAtmosphere(Mesh atmosphere, Camera3D cam){
        Gl.depthMask(false);
        Blending.additive.apply();

        batch.proj(cam.combined);
        drawEmissive();

        Blending.normal.apply();
        Gl.depthMask(true);

        if(lastParams != null){
            lastParams.alwaysDrawAtmosphere = wasAtmospheric;
            lastParams = null;
        }
    }

    @Override
    public Mat3D getTransform(Mat3D mat){
        return super.getTransform(mat).rotate(quat.set(Vec3.X, -90f));
    }

    public class SatelliteMesh implements GenericMesh{
        @Override
        public void render(PlanetParams params, Mat3D projection, Mat3D transform){
            var shader = CShaders.celestial;
            shader.light.set(solarSystem.position);
            shader.ambientColor.set(solarSystem.lightColor);
            shader.camPos.set(renderer.planets.cam.position);
            shader.bind();
            shader.apply();

            shader.setUniformMatrix4("u_proj", projection.val);
            drawStructure.get(shader, transform);
        }
    }
}
