package confictura.world.celestial;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g3d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import confictura.graphics.*;
import confictura.graphics.gl.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;

import static arc.Core.*;
import static mindustry.Vars.*;

/**
 * A black hole; inhabitable, bends light. Serves as the center of a solar system, and currently doesn't support having
 * a parent planet.
 * @author GlFolker
 */
public class BlackHole extends Planet{
    private static final Mat3D mat = new Mat3D();

    public float horizon = 0.36f;

    public @Nullable Mesh mesh;
    public @Nullable FrameBuffer ref;
    public @Nullable CFrameBuffer depth, depthRef;
    public @Nullable Cubemap skybox;

    protected Seq<Planet> stashChildren = new Seq<>(), requests = new Seq<>();
    protected Cubemap stashSkybox;
    protected boolean drawing = false;

    public BlackHole(String name, float radius){
        super(name, null, radius, 0);
        Events.run(Trigger.universeDrawBegin, () -> {
            if(ui.planet.state.planet.solarSystem == this){
                stashSkybox = Reflect.get(CubemapMesh.class, renderer.planets.skybox, "map");
                renderer.planets.skybox.setCubemap(skybox);
            }
        });

        Events.run(Trigger.universeDrawEnd, () -> {
            if(stashSkybox != null){
                renderer.planets.skybox.setCubemap(stashSkybox);
                stashSkybox = null;
            }

            if(!drawing) return;
            drawing = false;

            var stash = children;
            children = stashChildren;
            stashChildren = stash;
        });
    }

    @Override
    public void load(){
        super.load();
        if(mesh == null) mesh = MeshBuilder.buildIcosphere(3, radius, Color.white);
        if(ref == null) ref = new FrameBuffer(2, 2);
        if(depth == null) depth = new CFrameBuffer(2, 2, true);
        if(depthRef == null) depthRef = new CFrameBuffer(2, 2, true);
        if(skybox == null) {
            var base = "skyboxes/confictura/megalith/";
            skybox = new Cubemap(
                tree.get(base + "right.png"),
                tree.get(base + "left.png"),
                tree.get(base + "top.png"),
                tree.get(base + "bottom.png"),
                tree.get(base + "front.png"),
                tree.get(base + "back.png")
            );
        }
    }

    @Override
    public void draw(PlanetParams params, Mat3D projection, Mat3D transform){
        // Fool `PlanetRenderer` into thinking the black hole has no children, so that it can draw them itself.
        var stash = stashChildren;
        stashChildren = children;
        children = stash;

        drawing = true;
        var cam = renderer.planets.cam;
        //float prevFov = cam.fov;
        //cam.fov = Mathf.atan2(cam.far - cam.near, graphics.getWidth()) * Mathf.radDeg;
        //cam.update();

        depth.resize(graphics.getWidth(), graphics.getHeight());
        depthRef.resize(graphics.getWidth(), graphics.getHeight());

        {
            depthRef.begin(Color.clear);

            var shader = Shaders.unlit;
            shader.bind();
            shader.setUniformMatrix4("u_proj", projection.val);
            shader.setUniformMatrix4("u_trans", transform.val);
            shader.apply();

            mesh.render(shader, Gl.triangles);
            depthRef.end();
        }

        requests.clear();
        visit(this, requests::add);
        requests.sort(p -> -p.position.dst2(cam.position));

        int back = requests.indexOf(p -> p.position.dst2(cam.position) < position.dst2(cam.position));
        if(back == -1) back = requests.size;

        Intc2 draw = (begin, end) -> {
            for(int i = begin; i < end; i++){
                var planet = requests.get(i);
                if(!planet.visible()) continue;

                cam.update();
                if(cam.frustum.containsSphere(planet.position, planet.clipRadius)){
                    planet.draw(params, cam.combined, planet.getTransform(mat));
                }
            }

            for(int i = begin; i < end; i++){
                var planet = requests.get(i);
                if(!planet.visible()) continue;

                planet.drawClouds(params, cam.combined, planet.getTransform(mat));
                if(planet.hasGrid() && planet == params.planet && params.drawUi){
                    renderer.planets.renderSectors(planet, params);
                }

                if(cam.frustum.containsSphere(planet.position, planet.clipRadius) && planet.hasAtmosphere && (params.alwaysDrawAtmosphere || Core.settings.getBool("atmosphere"))){
                    planet.drawAtmosphere(renderer.planets.atmosphere, cam);
                }
            }
        };

        draw.get(0, back);

        depth.begin(Color.clear);
        for(var planet : requests){
            if(!planet.visible()) continue;

            renderer.planets.batch.proj(cam.combined);
            if(params.drawUi) renderer.planets.renderOrbit(planet, params);
        }
        depth.end();

        {
            var stencil = CShaders.blackHoleStencil;
            stencil.src = depth;
            stencil.ref = depthRef;
            Draw.blit(stencil);
        }

        {
            var buf = renderer.planets.bloom.buffer();
            ref.resize(buf.getWidth(), buf.getHeight());

            ref.begin();
            Draw.blit(buf.getTexture(), Shaders.screenspace);
            ref.end();
        }

        //cam.fov = prevFov;
        //cam.update();

        var shader = CShaders.blackHole;
        shader.camera = cam;
        shader.planet = this;
        shader.bind();
        shader.apply();
        mesh.render(shader, Gl.triangles);

        for(var planet : requests){
            if(!planet.visible()) continue;

            renderer.planets.batch.proj(cam.combined);
            if(params.drawUi) renderer.planets.renderOrbit(planet, params);
        }
        draw.get(back, requests.size);
    }

    @Override
    public void drawClouds(PlanetParams params, Mat3D projection, Mat3D transform){
        // Do nothing, it's already handled in `draw()`.
    }

    @Override
    public void drawAtmosphere(Mesh atmosphere, Camera3D cam){
        // The same.
    }

    protected void visit(Planet planet, Cons<Planet> cons){
        for(var child : (planet == this && drawing) ? stashChildren : planet.children){
            cons.get(child);
            visit(child, cons);
        }
    }
}