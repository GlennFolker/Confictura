package confictura.world.planets;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.graphics.g3d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import confictura.content.*;
import confictura.graphics.*;
import confictura.graphics.g3d.*;
import confictura.graphics.g3d.CMeshBuilder.*;
import confictura.util.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.graphics.g3d.PlanetGrid.*;
import mindustry.type.*;

import java.util.*;

import static arc.Core.*;
import static mindustry.Vars.*;

/**
 * The {@link CPlanets#portal portal} celestial object. Composed of floating islands, 9 sectors arranged on the surface,
 * and an artificial gravity forcefield.
 * @author GlennFolker
 */
public class PortalPlanet extends Planet{
    private static final Mat3D mat1 = new Mat3D(), mat2 = new Mat3D();
    private static final Quat quat = new Quat();
    private static final Vec3 v1 = new Vec3(), v2 = new Vec3(), v3 = new Vec3();
    private static final Intersect intersect = new Intersect();

    public Island[] islands = {};
    public float forcefieldRadius;

    public @Nullable Mesh atmosphereMesh;
    public Color atmosphereOutlineColor = new Color();

    public @Nullable FrameBuffer depthBuffer;

    public Cons2<Shader, Mat3D> drawStructure = (shader, transform) -> {};
    public Color sectorColor = Color.white;
    public float sectorOffset = 0f, sectorRadius = 1f, sectorInnerRadius = 2f, sectorDistance = 1f, sectorFade = 0.05f;

    public Cons<VertexBatch3D> drawEmissive = batch -> {};
    public float period = 60f;

    public @Nullable Texture emissiveTexture;
    public @Nullable TextureRegion[] emissiveRegions;
    public Color[] emissions = new Color[0];

    protected @Nullable VertexBatch3D batch;

    public static final int sectorSides = 8;

    protected static final float[] vertices = new float[sectorSides * 3];
    protected static final short[] indices = new short[(sectorSides - 2) * 3];

    public PortalPlanet(String name, Planet parent, float radius){
        super(name, parent, radius, 0);

        generateIcons = true;
        meshLoader = PortalMesh::new;
        forcefieldRadius = radius;
    }

    static{
        for(int i = 0, len = sectorSides - 2; i < len; i++){
            int index = i * 3;
            indices[index] = 0;
            indices[index + 1] = (short)(i + 1);
            indices[index + 2] = (short)(i + 2);
        }
    }

    @Override
    public void init(){
        grid = createSectorGrid();
        sectors.ensureCapacity(grid.tiles.length);
        for(var tile : grid.tiles) sectors.add(new Sector(this, tile){
            @Override
            protected SectorRect makeRect(){
                plane.set(tile.v, Vec3.Y);

                float offset = tile.id == 0 ? 0f : (-(tile.id - 1) * 360f / 8f);
                return new SectorRect(
                    sectorRadius,
                    tile.v.cpy(),
                    new Vec3(-1f, 0f, 0f).rotate(Vec3.Y, offset).setLength(sectorRadius),
                    new Vec3(0f, 0f, -1f).rotate(Vec3.Y, offset).setLength(sectorRadius),
                    0f
                );
            }
        });

        sectorApproxRadius = sectors.first().tile.v.dst(sectors.first().tile.corners[0].v);
        gridMeshLoader = () -> CMeshBuilder.gridLines(grid, sectorColor);
        super.init();
    }

    @Override
    public void load(){
        super.load();
        if(!headless){
            if(atmosphereMesh == null) atmosphereMesh = CMeshBuilder.gridDistance(PlanetGrid.create(3), atmosphereOutlineColor, 1f);
            if(depthBuffer == null){
                depthBuffer = new FrameBuffer(graphics.getWidth(), graphics.getHeight(), true);
                depthBuffer.getTexture().setFilter(TextureFilter.nearest);
            }

            // HACK Obviously this is a problem with Arc mishandling vertex attribute offsets.
            if(batch == null) batch = new VertexBatch3D(4096, true, true, 1, CShaders.portalBatch){
                @Override
                public void color(Color color){
                    Reflect.<float[]>get(VertexBatch3D.class, this, "vertices")[Reflect.<Integer>get(VertexBatch3D.class, this, "vertexIdx") + 6] = color.toFloatBits();
                }

                @Override
                public void color(float r, float g, float b, float a){
                    Reflect.<float[]>get(VertexBatch3D.class, this, "vertices")[Reflect.<Integer>get(VertexBatch3D.class, this, "vertexIdx") + 6] = Color.toFloatBits(r, g, b, a);
                }

                @Override
                public void color(float colorBits){
                    Reflect.<float[]>get(VertexBatch3D.class, this, "vertices")[Reflect.<Integer>get(VertexBatch3D.class, this, "vertexIdx") + 6] = colorBits;
                }

                @Override
                public void texCoord(float u, float v){
                    int idx = Reflect.<Integer>get(VertexBatch3D.class, this, "vertexIdx") + 7,
                        num = Reflect.<Integer>get(VertexBatch3D.class, this, "numSetTexCoords");

                    float[] vertices = Reflect.get(VertexBatch3D.class, this, "vertices");
                    vertices[idx + num] = u;
                    vertices[idx + num + 1] = v;
                    Reflect.set(VertexBatch3D.class, this, "numSetTexCoords", num + 2);
                }
            };

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

    public PlanetGrid createSectorGrid(){
        var grid = new PlanetGrid(0){};
        grid.tiles = new Ptile[9];
        grid.edges = new Edge[9 * sectorSides];
        grid.corners = new Corner[9 * sectorSides];

        float step = 360f / sectorSides, offset = -step / 2f;
        for(int i = 0; i < 9; i++){
            var tile = grid.tiles[i] = new Ptile(i, sectorSides);
            tile.tiles = new Ptile[0];

            for(int j = 0; j < sectorSides; j++){
                int base = i * sectorSides;

                var corner = grid.corners[base + j] = tile.corners[j] = new Corner(base + j);
                corner.tiles = new Ptile[]{tile};
                corner.edges = new Edge[2];

                var edge = grid.edges[base + j] = tile.edges[j] = new Edge(base + j);
                edge.tiles = new Ptile[]{tile};
            }

            for(int j = 0; j < sectorSides; j++){
                var corner = tile.corners[j];
                var edge = tile.edges[j];

                corner.corners = new Corner[]{
                    tile.corners[Mathf.mod(j - 1, sectorSides)],
                    tile.corners[Mathf.mod(j + 1, sectorSides)]
                };
                corner.edges = new Edge[]{
                    tile.edges[Mathf.mod(j - 1, sectorSides)],
                    edge
                };

                edge.corners = new Corner[]{
                    corner,
                    tile.corners[Mathf.mod(j + 1, sectorSides)]
                };
            }
        }

        for(int i = -1; i < 8; i++){
            var tile = grid.tiles[i + 1];
            if(i == -1){
                Tmp.v1.setZero();
            }else{
                Tmp.v1.trns(-i * 45f, sectorDistance);
            }

            tile.v.set(Tmp.v1.x, sectorOffset, Tmp.v1.y);
            for(int j = 0; j < sectorSides; j++){
                Tmp.v2.trns(offset - j * step, i == -1 ? sectorInnerRadius : sectorRadius);
                tile.corners[j].v.set(tile.v).add(Tmp.v2.x, 0f, Tmp.v2.y);
            }
        }

        return grid;
    }

    @Override
    public boolean hasGrid(){
        return true;
    }

    @Override
    public @Nullable Sector getSector(Ray ray, float radius){
        var intersect = intersect(ray, radius);
        if(intersect == null || intersect.intersected == null) return null;

        return sectors.get(intersect.intersected.id);
    }

    @Override
    public @Nullable Intersect intersect(Ray ray, float radius){
        for(var tile : grid.tiles){
            for(int i = 0; i < sectorSides; i++){
                intersect.set(tile.corners[i].v).rotate(Vec3.Y, -getRotation()).add(position);

                int index = i * 3;
                vertices[index] = intersect.x;
                vertices[index + 1] = intersect.y;
                vertices[index + 2] = intersect.z;
            }

            if(Intersector3D.intersectRayTriangles(ray, vertices, indices, 3, intersect)){
                intersect.intersected = tile;
                return intersect;
            }
        }

        for(int i = 0; i < sectorSides; i++){
            intersect
                .set(grid.tiles[0].corners[i].v).add(0f, -sectorOffset, 0f)
                .rotate(Vec3.Y, -getRotation())
                .setLength(sectorDistance + sectorRadius)
                .add(position.x, position.y + sectorOffset, position.z);

            int index = i * 3;
            vertices[index] = intersect.x;
            vertices[index + 1] = intersect.y;
            vertices[index + 2] = intersect.z;
        }

        if(Intersector3D.intersectRayTriangles(ray, vertices, indices, 3, intersect)){
            intersect.intersected = null;
            return intersect;
        }else{
            return null;
        }
    }

    @Override
    public void drawBorders(VertexBatch3D batch, Sector sector, Color base, float alpha){
        // I apologize for the performance loss...
        batch.flush(Gl.triangles);

        var color = Tmp.c1.set(sectorColor).a((base.a + 0.3f + Mathf.absin(Time.globalTime, 5f, 0.3f)) * alpha);
        var fade = Tmp.c2.set(color).a(0f);

        var corners = sector.tile.corners;
        for(int i = 0; i < corners.length; i++){
            Corner curr = corners[i], next = corners[(i + 1) % corners.length];

            v1.set(curr.v);
            v2.set(next.v);
            v3.set(curr.v).sub(0f, sectorFade, 0f);

            batch.color(color);
            batch.vertex(v1);
            batch.color(color);
            batch.vertex(v2);
            batch.color(fade);
            batch.vertex(v3);

            batch.color(color);
            batch.vertex(v1);
            batch.color(fade);
            batch.vertex(v3);
            batch.color(color);
            batch.vertex(v2);

            v1.set(next.v);
            v2.set(next.v).sub(0f, sectorFade, 0f);
            v3.set(curr.v).sub(0f, sectorFade, 0f);

            batch.color(color);
            batch.vertex(v1);
            batch.color(fade);
            batch.vertex(v2);
            batch.color(fade);
            batch.vertex(v3);

            batch.color(color);
            batch.vertex(v1);
            batch.color(fade);
            batch.vertex(v3);
            batch.color(fade);
            batch.vertex(v2);
        }

        // ... once again I apologize...
        Gl.depthMask(false);
        batch.flush(Gl.triangles);
        Gl.depthMask(true);
    }

    @Override
    public void drawSelection(VertexBatch3D batch, Sector sector, Color color, float stroke, float length){
        stroke /= 2f;

        var tile = sector.tile;
        var corners = tile.corners;
        for(int i = 0; i < corners.length; i++){
            Corner curr = corners[i], next = corners[(i + 1) % corners.length];

            v1.set(curr.v);
            v2.set(next.v);
            v3.set(curr.v).sub(tile.v);
            v3.setLength(v3.len() - stroke).add(tile.v);
            batch.tri2(v1, v2, v3, sectorColor);

            v1.set(v3);
            v2.set(next.v);
            v3.set(next.v).sub(tile.v);
            v3.setLength(v3.len() - stroke).add(tile.v);
            batch.tri2(v1, v2, v3, sectorColor);
        }
    }

    @Override
    public void fill(VertexBatch3D batch, Sector sector, Color color, float offset){
        var corners = sector.tile.corners;
        for(int i = 0; i < sectorSides - 2; i++){
            Corner a = corners[0], b = corners[i + 1], c = corners[i + 2];
            batch.tri2(
                Tmp.v31.set(a.v).add(0f, offset, 0f),
                Tmp.v32.set(b.v).add(0f, offset, 0f),
                Tmp.v33.set(c.v).add(0f, offset, 0f),
                // HACK: Check if the color is equal to the locked color to see if the sector is locked.
                color.equals(PlanetRenderer.shadowColor) ? color : Tmp.c1.set(sectorColor).mulA(color.a * 0.67f)
            );
        }
    }

    @Override
    public void renderSectors(VertexBatch3D batch, Camera3D cam, PlanetParams params){
        batch.proj().mul(getTransform(mat1));
        if(params.renderer != null) params.renderer.renderSectors(this);

        var shader = Shaders.planetGrid;
        var tile = intersect(cam.getMouseRay(), radius);
        shader.mouse.lerp(tile == null ? Tmp.v31.set(0f, sectorOffset + 1f, 0f) : tile.sub(position).rotate(Vec3.Y, getRotation()), 0.2f);

        shader.bind();
        shader.setUniformMatrix4("u_proj", cam.combined.val);
        shader.setUniformMatrix4("u_trans", getTransform(mat1).val);
        shader.apply();
        gridMesh.render(shader, Gl.lines);
    }

    public float period(Interp interpolation){
        return period(0f, 0f, 1f, interpolation);
    }

    public float period(float offset, float from, float to, Interp interpolation){
        float frac = (Time.globalTime + offset * period) / period;
        return interpolation.apply(Mathf.curve(frac % 1f, from, to));
    }

    public void drawEmissive(){
        CShaders.portalBatch.planet = this;

        drawEmissive.get(batch);
        batch.flush(Gl.triangles);
    }

    @Override
    public void drawAtmosphere(Mesh atmosphere, Camera3D cam){
        Gl.depthMask(false);
        Blending.additive.apply();

        batch.proj(cam.combined);
        drawEmissive();

        var shader = CShaders.portalForcefield;
        shader.camera = cam;
        shader.planet = this;

        shader.bind();
        shader.apply();
        atmosphereMesh.render(shader, Gl.triangles);

        Blending.normal.apply();
        Gl.depthMask(true);
    }

    @Override
    public Vec3 lookAt(Sector sector, Vec3 out){
        if(sector.id == 0){
            out.set(1f, -sectorOffset, 0f).nor().rotate(Vec3.Y, -getRotation());
        }else{
            out.set(sector.tile.v).add(0f, -sectorOffset, 0f).rotate(Vec3.Y, -getRotation());
        }

        return out;
    }

    @Override
    public Vec3 project(Sector sector, Camera3D cam, Vec3 out){
        return cam.project(out.set(sector.tile.v).rotate(Vec3.Y, -getRotation()).add(position));
    }

    @Override
    public void setPlane(Sector sector, PlaneBatch3D projector){
        float rot = -getRotation();
        projector.setPlane(
            Tmp.v31.set(sector.rect.center).add(0f, 0.02f, 0f).rotate(Vec3.Y, rot).add(position),
            Tmp.v32.set(sector.rect.top).rotate(Vec3.Y, rot),
            Tmp.v33.set(sector.rect.right).rotate(Vec3.Y, rot)
        );
    }

    public static class Island{
        public float radius;
        public float resolution;
        public IslandShaper shaper;

        public final Vec3 offset = new Vec3();
        public float rotation;
        public float hoverMag, hoverScale;

        public Island(float radius, IslandShaper shaper){
            this(radius, 50f, shaper);
        }

        public Island(float radius, float resolution, IslandShaper shaper){
            this.radius = radius;
            this.resolution = resolution;
            this.shaper = shaper;
        }
    }

    public class PortalMesh implements GenericMesh{
        public Mesh[] islandMeshes;

        public PortalMesh(){
            islandMeshes = new Mesh[islands.length];
            for(int i = 0; i < islands.length; i++){
                var island = islands[i];
                islandMeshes[i] = CMeshBuilder.island(island.radius, island.resolution, island.shaper);
            }
        }

        @Override
        public void render(PlanetParams params, Mat3D projection, Mat3D transform){
            if(params.alwaysDrawAtmosphere || settings.getBool("atmosphere")){
                depthBuffer.resize(graphics.getWidth(), graphics.getHeight());
                depthBuffer.begin(Tmp.c1.set(0xffffff00));
                Blending.disabled.apply();

                render(() -> {
                    var shader = CShaders.depth;
                    shader.camera = renderer.planets.cam;
                    return shader;
                }, projection, transform);

                Blending.normal.apply();
                depthBuffer.end();
            }

            render(() -> {
                var shader = CShaders.celestial;
                shader.light.set(solarSystem.position);
                shader.ambientColor.set(solarSystem.lightColor);
                shader.camPos.set(renderer.planets.cam.position);
                return shader;
            }, projection, transform);
        }

        protected void render(Prov<? extends Shader> shaderProv, Mat3D projection, Mat3D transform){
            var shader = shaderProv.get();
            shader.bind();
            shader.apply();

            shader.setUniformMatrix4("u_proj", projection.val);
            drawStructure.get(shader, transform);

            for(int i = 0, len = islands.length; i < len; i++){
                var island = islands[i];
                var mesh = islandMeshes[i];

                mat1.set(island.offset, quat.setFromAxis(Vec3.Y, island.rotation));
                mat2.set(transform).mul(mat1);

                mat1.set(v1.set(0f, -Mathf.absin(Time.globalTime, island.hoverScale, island.hoverMag), 0f), quat.idt());
                mat2.mul(mat1);

                shader.setUniformMatrix4("u_trans", mat2.val);
                shader.setUniformMatrix("u_normal", MathUtils.copyMatrix(mat2, Tmp.m1).inv().transpose());
                mesh.render(shader, Gl.triangles);
            }
        }
    }

    public static class Intersect extends Vec3{
        public @Nullable Ptile intersected;
    }
}
