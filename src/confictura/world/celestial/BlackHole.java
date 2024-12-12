package confictura.world.celestial;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.graphics.g3d.*;
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
    private static final Vec3 v1 = new Vec3(), v2 = new Vec3(), v3 = new Vec3();
    private static final Camera3D frustum = new Camera3D();

    public float horizon = 0.4f;

    public @Nullable Cubemap skybox;
    public @Nullable CFrameBufferCubemap pov;

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
        if(skybox == null){
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

        if(pov == null){
            pov = new CFrameBufferCubemap(2, 2, true);
            pov.getTexture().setFilter(TextureFilter.nearest);
        }
    }

    @Override
    public void draw(PlanetParams params, Mat3D projection, Mat3D transform){
        renderer.planets.bloom.setBloomIntensity(0f);
        // Fool `PlanetRenderer` into thinking the black hole has no children, so that it can draw them itself.
        var stash = stashChildren;
        stashChildren = children;
        children = stash;

        drawing = true;

        int dim = Math.max(graphics.getWidth(), graphics.getHeight());
        var cam = renderer.planets.cam;
        float fov = cam.fov, w = cam.width, h = cam.height;
        var up = v1.set(cam.up);
        var dir = v2.set(cam.direction);
        cam.fov = 90f;
        cam.width = cam.height = dim;

        frustum.position.set(position);
        frustum.fov = 30f;
        frustum.width = frustum.height = 1f;
        frustum.lookAt(cam.position);
        frustum.update();

        requests.clear();
        visit(this, requests::add);

        var shader = CShaders.rayBlackHole;
        pov.resize(dim, dim);
        pov.begin();
        pov.eachSide(side -> {
            Gl.clearColor(0f, 0f, 0f, 0f);
            Gl.clear(Gl.colorBufferBit | Gl.depthBufferBit);

            side.getUp(cam.up);
            side.getDirection(cam.direction);

            if(params.drawSkybox){
                var lastPos = v3.set(cam.position);
                cam.position.setZero();
                cam.update();

                Gl.depthMask(false);
                renderer.planets.skybox.render(cam.combined);
                Gl.depthMask(true);

                cam.position.set(lastPos);
            }

            cam.update();
            shader.cubemapView[side.index].set(cam.view);

            for(var p : requests){
                if(!p.visible()) continue;
                if(cam.frustum.containsSphere(p.position, p.clipRadius) && !frustum.frustum.containsSphere(p.position, p.clipRadius)){
                    p.draw(params, cam.combined, p.getTransform(mat));
                }
            }

            for(var p : requests){
                if(!p.visible()) continue;
                if(!frustum.frustum.containsSphere(p.position, p.clipRadius)){
                    p.drawClouds(params, cam.combined, p.getTransform(mat));
                    if(p.hasGrid() && p == params.planet && params.drawUi){
                        renderer.planets.renderSectors(p, params);
                    }

                    if(cam.frustum.containsSphere(p.position, p.clipRadius) && p.parent != null && p.hasAtmosphere && (params.alwaysDrawAtmosphere || Core.settings.getBool("atmosphere"))){
                        p.drawAtmosphere(renderer.planets.atmosphere, cam);
                    }
                }
            }

            for(var p : requests){
                if(!p.visible()) continue;
                if(params.drawUi){
                    renderer.planets.batch.proj(cam.combined);
                    renderer.planets.renderOrbit(p, params);
                }
            }
        });
        pov.end();

        cam.up.set(up);
        cam.direction.set(dir);
        cam.fov = fov;
        cam.width = w;
        cam.height = h;
        cam.update();

        shader.camera = cam;
        shader.planet = this;
        Draw.blit(shader);

        for(var child : stashChildren) renderer.planets.renderPlanet(child, params);
        for(var child : stashChildren) renderer.planets.renderTransparent(child, params);

        /*var cam = renderer.planets.cam;
        float fov = cam.fov;
        cam.fov = (float)(Math.atan(2 * Math.tan(fov * Mathf.doubleDegRad)) * Mathf.doubleRadDeg);
        cam.update();

        big.resize(graphics.getWidth() * 2, graphics.getHeight() * 2);
        big.begin(Color.clear);

        if(params.drawSkybox){
            //render skybox at 0,0,0
            Vec3 lastPos = Tmp.v31.set(cam.position);
            cam.position.setZero();
            cam.update();

            Gl.depthMask(false);

            renderer.planets.skybox.render(cam.combined);

            Gl.depthMask(true);

            cam.position.set(lastPos);
            cam.update();
        }

        depth.resize(graphics.getWidth() * 2, graphics.getHeight() * 2);
        depthRef.resize(graphics.getWidth() * 2, graphics.getHeight() * 2);

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
            var buf = big;//renderer.planets.bloom.buffer();
            ref.resize(buf.getWidth(), buf.getHeight());

            ref.begin();
            Draw.blit(buf.getTexture(), CShaders.screenspace.trns(0.5f, 0.5f, 1f, 1f));
            ref.end();
        }

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

        big.end();
        cam.fov = fov;
        cam.update();

        Draw.blit(big.getTexture(), CShaders.screenspace.trns(0.5f, 0.5f, 1f / 2f, 1f / 2f));*/
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