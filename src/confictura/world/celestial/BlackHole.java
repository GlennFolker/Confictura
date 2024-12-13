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

    public @Nullable Mesh mesh;
    public @Nullable Cubemap skybox;
    public @Nullable CFrameBufferCubemap pov;
    public @Nullable CFrameBuffer orbit, orbitRef;

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
        if(mesh == null) mesh = MeshBuilder.buildIcosphere(3, radius);

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

        if(orbit == null){
            orbit = new CFrameBuffer(2, 2, true);
            orbit.getTexture().setFilter(TextureFilter.nearest);
        }

        if(orbitRef == null) orbitRef = new CFrameBuffer(2, 2, true);
    }

    @Override
    public void draw(PlanetParams params, Mat3D projection, Mat3D transform){
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

        orbit.resize(dim, dim);
        orbitRef.resize(dim, dim);

        var shader = CShaders.blackHole;
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

            orbitRef.begin(Color.clear);

            var unlit = Shaders.unlit;
            unlit.bind();
            unlit.setUniformMatrix4("u_proj", cam.combined.val);
            unlit.setUniformMatrix4("u_trans", getTransform(mat).val);
            unlit.apply();

            mesh.render(unlit, Gl.triangles);
            orbitRef.end();

            orbit.begin(Color.clear);
            for(var p : requests){
                if(!p.visible()) continue;
                if(params.drawUi){
                    renderer.planets.batch.proj(cam.combined);
                    renderer.planets.renderOrbit(p, params);
                }
            }
            orbit.end();

            var stencil = CShaders.blackHoleStencil;
            stencil.src = orbit;
            stencil.ref = orbitRef;
            Draw.blit(stencil);
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