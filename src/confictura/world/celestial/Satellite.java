package confictura.world.celestial;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g3d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import confictura.graphics.*;
import confictura.graphics.g3d.*;
import gltfrenzy.model.*;
import mindustry.game.EventType.*;
import mindustry.graphics.g3d.*;
import mindustry.graphics.g3d.PlanetGrid.*;
import mindustry.maps.generators.*;
import mindustry.type.*;
import mindustry.world.meta.*;

import static confictura.graphics.CPal.*;
import static confictura.util.MathUtils.*;
import static confictura.util.StructUtils.*;
import static mindustry.Vars.*;

public class Satellite extends EmissiveObject{
    private static final Mat3D mat = new Mat3D();
    private static final Quat quat = new Quat();
    private static final Vec3 nor = new Vec3();

    private static float lastSpacing;

    public float scale = 0.0175f;

    public Satellite(String name, Planet observed, float distance){
        super(name, distance(observed, distance), 0.1f);
        undistance(observed);

        updateLighting = drawOrbit = false;
        defaultEnv = Env.space;
        meshLoader = SatelliteMesh::new;
        tidalLock = true;
        camRadius = 1.3f;

        emissions = new Color[]{monolithDark, monolithDarker};
        sectors.add(new Sector(this, Ptile.empty));
        generator = new BlankPlanetGenerator();

        // Deliberately make it as the first child to get as close as possible.
        // This *might* be invasive for mods that are sensitive towards their planet children indices (which I can
        // literally find no reason for), however until something weird happens, I will keep this code.
        Events.on(ContentInitEvent.class, e -> {
            var c = parent.children;

            int index = c.indexOf(this);
            if(index == 0) return;

            float delta = orbitRadius - c.get(index - 1).orbitRadius;
            c.remove(index);
            c.insert(0, this);

            orbitRadius = parent.radius + distance + totalRadius;
            orbitTime = 150f;
            clipRadius = totalRadius + distance + parent.radius * 2f;

            for(int i = 1; i <= index; i++) c.get(i).orbitRadius += delta;
            parent.updateTotalRadius();
        });
    }

    protected static Planet distance(Planet planet, float spacing){
        lastSpacing = planet.orbitSpacing;
        planet.orbitSpacing = spacing;
        return planet;
    }

    protected static void undistance(Planet planet){
        planet.orbitSpacing = lastSpacing;
    }

    public float period(float period, Interp interpolation){
        return period(period, 0f, 0f, 1f, interpolation);
    }

    public float period(float period, float offset, float from, float to, Interp interpolation){
        float frac = (Time.globalTime + offset * period) / period;
        return interpolation.apply(Mathf.curve(frac % 1f, from, to));
    }

    @Override
    public void drawEmissive(){
        TextureRegion beam = emissiveRegions[0], shade = emissiveRegions[1];
        for(int i = 0; i < 3; i++){
            float time = period(240f, i / 3f, 0f, 1f, Interp.linear);
            float rot = Interp.pow2Out.apply(time) * 180f;
            float rad = Interp.pow2Out.apply(1f - time) * 8f;
            float offset = (Interp.pow2Out.apply(1f - time) - 1f) * 8f - 1f - absin(period(168f, Interp.linear) * Mathf.PI2, 1f, 1f);
            float col = Interp.pow2Out.apply(Mathf.curve(time, 0f, 0.24f)) - Interp.pow2In.apply(Mathf.curve(time, 0.24f, 1f));

            for(int j = 0; j < 8; j++){
                Tmp.v1.trns(rot + j * 45f, rad);
                Tmp.v2.trns(rot + j * 45f, Math.max(rad - 1f, 0f));
                Tmp.v3.trns(rot + (j + 1) * 45f, rad);
                Tmp.v4.trns(rot + (j + 1) * 45f, Math.max(rad - 1f, 0f));

                Tmp.v31.set(Tmp.v1.x, offset, -Tmp.v1.y);
                Tmp.v32.set(Tmp.v3.x, offset, -Tmp.v3.y);
                Tmp.v33.set(Tmp.v4.x, offset, -Tmp.v4.y);
                Tmp.v34.set(Tmp.v2.x, offset, -Tmp.v2.y);

                normal(nor, Tmp.v31, Tmp.v32, Tmp.v33);
                Tmp.c1.set(monolithMid).a(col);

                Draw3DUtils.quad2(
                    batch,
                    Tmp.v31, nor, Tmp.c1, beam.u, beam.v2,
                    Tmp.v32, nor, Tmp.c1, beam.u2, beam.v2,
                    Tmp.v33, nor, Tmp.c1, beam.u2, beam.v,
                    Tmp.v34, nor, Tmp.c1, beam.u, beam.v
                );

                Tmp.v33.set(Tmp.v32).y -= 1f;
                Tmp.v34.set(Tmp.v31).y -= 1f;

                normal(nor, Tmp.v33, Tmp.v32, Tmp.v31);
                Tmp.c1.a /= 2f;
                Tmp.c2.set(Tmp.c1).a(0f);

                Draw3DUtils.quad2(
                    batch,
                    Tmp.v34, nor, Tmp.c2, beam.u, beam.v,
                    Tmp.v33, nor, Tmp.c2, beam.u2, beam.v,
                    Tmp.v32, nor, Tmp.c1, beam.u2, beam.v2,
                    Tmp.v31, nor, Tmp.c1, beam.u, beam.v2
                );
            }
        }

        for(int i = 0; i < 4; i++){
            float rad = Mathf.sqrt2;
            float thrust = 0.64f + absin(period(120f, Interp.linear) * Mathf.PI2) * 0.36f;

            Tmp.v1.trns(45f + i * 90f, rad);
            Tmp.v2.trns(45f + (i + 1f) * 90f, rad);

            Tmp.v31.set(Tmp.v1.x, 0f, Tmp.v1.y);
            Tmp.v32.set(Tmp.v2.x, 0f, Tmp.v2.y);
            Tmp.v33.set(Tmp.v32.x, -thrust * 10f, Tmp.v32.z);
            Tmp.v34.set(Tmp.v31.x, Tmp.v33.y, Tmp.v31.z);

            normal(nor, Tmp.v31, Tmp.v32, Tmp.v33);
            Tmp.c1.set(monolithDark).a(1f - thrust * 0.5f);
            Tmp.c2.set(Tmp.c1).a(0f);

            Draw3DUtils.quad2(
                batch,
                Tmp.v31, nor, Tmp.c1, shade.u, shade.v2,
                Tmp.v32, nor, Tmp.c1, shade.u2, shade.v2,
                Tmp.v33, nor, Tmp.c2, shade.u2, shade.v,
                Tmp.v34, nor, Tmp.c2, shade.u, shade.v
            );
        }
    }

    @Override
    public void drawActualAtmosphere(Mesh atmosphere, Camera3D cam){}

    @Override
    public Mat3D getTransform(Mat3D mat){
        return super.getTransform(mat).rotate(quat.set(Vec3.X, -90f)).scale(scale, scale, scale);
    }

    public class SatelliteMesh implements GenericMesh{
        @Override
        public void render(PlanetParams params, Mat3D projection, Mat3D transform){
            Node base = CModels.satelliteBase, thruster = CModels.satelliteThruster, inner = CModels.satelliteArmInner, outer = CModels.satelliteArmOuter;
            base.localTrns.translation.set(0f, -absin(period(345f, Interp.linear) * Mathf.PI2, 1f, 0.5f) * scale, 0f);
            base.localTrns.rotation.idt();
            base.localTrns.scale.set(1f, 1f, 1f);

            thruster.localTrns.translation.set(0f, -absin(period(168f, Interp.linear) * Mathf.PI2, 1f, 1f), 0f);
            thruster.localTrns.rotation.idt();
            thruster.localTrns.scale.set(1f, 1f, 1f);

            inner.localTrns.translation.set(0f, -absin(period(128f, Interp.linear) * Mathf.PI2, 1f, 2f), 0f);
            inner.localTrns.rotation.set(Vec3.Y, period(320f, Interp.pow2) * 360f);
            inner.localTrns.scale.set(1f, 1f, 1f);

            outer.localTrns.translation.set(0f, absin(period(128f, Interp.linear) * Mathf.PI2, 1f, 2f), 0f);
            outer.localTrns.rotation.set(Vec3.Y, -period(320f, Interp.pow2) * 360f);
            outer.localTrns.scale.set(1f, 1f, 1f);
            base.update();

            var shader = CShaders.celestial;
            shader.light.set(solarSystem.position);
            shader.ambientColor.set(solarSystem.lightColor);
            shader.camPos.set(renderer.planets.cam.position);
            shader.bind();
            shader.apply();

            shader.setUniformMatrix4("u_proj", projection.val);
            for(var node : iter(base, thruster, inner, outer)){
                shader.setUniformMatrix4("u_trans", mat.set(transform).mul(node.globalTrns).val);
                shader.setUniformMatrix("u_normal", copyMatrix(mat, Tmp.m1).inv().transpose());

                node.mesh.containers.each(mesh -> mesh.render(shader));
            }
        }
    }
}
