package confictura.world.celestial;

import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.graphics.g3d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.noise.*;
import confictura.content.*;
import confictura.graphics.*;
import confictura.graphics.g3d.*;
import confictura.graphics.g3d.CMeshBuilder.*;
import confictura.graphics.gl.*;
import gltfrenzy.model.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.graphics.g3d.PlanetGrid.*;
import mindustry.maps.generators.*;
import mindustry.type.*;

import static arc.Core.*;
import static confictura.graphics.CPal.*;
import static confictura.util.MathUtils.*;
import static confictura.util.StructUtils.*;
import static mindustry.Vars.*;

/**
 * The {@linkplain CPlanets#portal portal} celestial object. Composed of floating islands, 9 sectors arranged on the surface,
 * and an artificial gravity forcefield.
 * @author GlFolker
 */
public class Portal extends EmissiveObject{
    private static final Mat3D mat1 = new Mat3D(), mat2 = new Mat3D();
    private static final Quat quat = new Quat();
    private static final Vec3 v1 = new Vec3(), v2 = new Vec3(), v3 = new Vec3(), nor = new Vec3();
    private static final Color c1 = new Color(), c2 = new Color();
    private static final Intersect intersect = new Intersect();

    public static final Color sectorColor = monolithLighter;
    public static final float
        sectorOffset = -0.15f, sectorRadius = 0.08f, sectorInnerRadius = 0.15f, sectorDistance = 0.275f, sectorFade = 0.05f,
        structureOffset = -0.2125f, structureScale = 0.05f,
        period = 240f;

    public Island[] islands;
    public float forcefieldRadius;

    public @Nullable Mesh atmosphereMesh;
    public Color atmosphereOutlineColor = new Color();

    public @Nullable CFrameBuffer buffer;

    public static final int sectorSides = 8;

    protected static final float[] vertices = new float[sectorSides * 3];
    protected static final short[] indices = new short[(sectorSides - 2) * 3];

    static{
        for(int i = 0, len = sectorSides - 2; i < len; i++){
            int index = i * 3;
            indices[index] = 0;
            indices[index + 1] = (short)(i + 1);
            indices[index + 2] = (short)(i + 2);
        }
    }

    public Portal(String name, Planet parent, float radius){
        super(name, parent, radius);

        generateIcons = true;
        meshLoader = PortalMesh::new;
        forcefieldRadius = radius;
        hasAtmosphere = true;

        emissions = new Color[]{monolithMid, monolithDark};
        atmosphereColor.set(0x3366e5ff);
        atmosphereOutlineColor.set(0x1966ffff);

        icon = "host";
        iconColor = monolithLighter;
        camRadius = -0.067f;
        minZoom = 0.75f;

        islands = createIslands();

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

        generator = new BlankPlanetGenerator();
    }

    @Override
    public void load(){
        super.load();
        if(!headless){
            if(atmosphereMesh == null) atmosphereMesh = CMeshBuilder.gridDistance(PlanetGrid.create(3), atmosphereOutlineColor, 1f);
            if(buffer == null){
                buffer = new CFrameBuffer(2, 2, true);
                buffer.getTexture().setFilter(TextureFilter.nearest);
            }
        }
    }

    public Island[] createIslands(){
        IslandBuilder island = (int seed, float dstScale, float depth, float depthScale, Color stroke) -> {
            int seeded = seed * 4;
            return (x, y, hex) -> {
                float dst = Mathf.dst(x, y) / dstScale;
                hex.low = depth + dst * depthScale + Simplex.noise2d(seeded, 3f, 0.5f, 4.5f, x + 31.41f, y + 59.26f) * (0.3f + Interp.pow2In.apply(dst) * 0.7f) * 0.1f;
                hex.high = -Interp.pow3In.apply(dst) * 0.067f + Simplex.noise2d(seeded + 1, 3f, 0.5f, 4f, x + 53.58f, y + 97.93f) * (0.4f + Interp.pow2In.apply(dst) * 0.6f) * 0.08f;

                hex.lowColor.set(Pal.darkerGray)
                    .lerp(Pal.stoneGray, Interp.pow4In.apply(Simplex.noise3d(seeded + 2, 3f, 0.3f, 5f, x + 31.41f, y + 59.26f, hex.low) / 2f + 0.5f))
                    .lerp(stroke.r, stroke.g, stroke.b, 0f, Interp.pow3In.apply(Ridged.noise3d(seeded + 3, x, y, hex.low, 1, 8f) / 2f + 0.5f));
                hex.highColor.set(Pal.darkerGray)
                    .lerp(Pal.stoneGray, Interp.pow4In.apply(Simplex.noise3d(seeded + 2, 3f, 0.3f, 5f, x + 31.41f, y + 59.26f, hex.high) / 2f + 0.5f))
                    .lerp(stroke.r, stroke.g, stroke.b, 0f, Interp.pow3In.apply(Ridged.noise3d(seeded + 3, x, y, hex.high, 1, 8f) / 2f + 0.5f));
            };
        };

        return new Island[]{
            new Island(0.4f, island.get(0, 0.35f, -0.55f, 0.45f, monolithMid)){{
                offset.set(0f, -0.23f, 0f);
            }},
            new Island(0.4f, island.get(1, 0.18f, -0.2f, 0.56f, monolithDarker)){{
                offset.set(0.32f, -0.35f, 0.2f);
                rotation = 60f;
                hoverScale = 100f;
                hoverMag = 0.06f;
            }},
            new Island(0.4f, island.get(2, 0.2f, -0.27f, 0.5f, monolithDark)){{
                offset.set(0.3f, -0.47f, -0.18f);
                rotation = 45f;
                hoverScale = 80f;
                hoverMag = 0.05f;
            }},
            new Island(0.4f, island.get(3, 0.16f, -0.22f, 0.47f, monolithMid)){{
                offset.set(-0.1f, -0.44f, 0.36f);
                rotation = 30f;
                hoverScale = 70f;
                hoverMag = 0.04f;
            }},
            new Island(0.4f, island.get(4, 0.17f, -0.2f, 0.35f, monolithLight)){{
                offset.set(-0.32f, -0.5f, 0f);
                rotation = 75f;
                hoverScale = 60f;
                hoverMag = 0.034f;
            }},
            new Island(0.4f, island.get(5, 0.16f, -0.24f, 0.4f, monolithLighter)){{
                offset.set(-0.23f, -0.4f, -0.32f);
                rotation = 75f;
                hoverScale = 60f;
                hoverMag = 0.04f;
            }}
        };
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

        var color = c1.set(sectorColor).a((base.a + 0.3f + Mathf.absin(Time.globalTime, 5f, 0.3f)) * alpha);
        var fade = c2.set(color).a(0f);

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
        c1.set(sectorColor).a(color.a);
        stroke /= 2f;

        var tile = sector.tile;
        var corners = tile.corners;
        for(int i = 0; i < corners.length; i++){
            Corner curr = corners[i], next = corners[(i + 1) % corners.length];

            v1.set(curr.v);
            v2.set(next.v);
            v3.set(curr.v).sub(tile.v);
            v3.setLength(v3.len() - stroke).add(tile.v);
            batch.tri2(v1, v2, v3, c1);

            v1.set(v3);
            v2.set(next.v);
            v3.set(next.v).sub(tile.v);
            v3.setLength(v3.len() - stroke).add(tile.v);
            batch.tri2(v1, v2, v3, c1);
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
                color.equals(PlanetRenderer.shadowColor) ? color : c1.set(sectorColor).mulA(color.a * 0.67f)
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

    @Override
    public void drawEmissive(){
        TextureRegion beam = emissiveRegions[0], shade = emissiveRegions[1];
        for(int i = 0; i < 8; i++){
            float stroke = period(0.5f, 0f, 0.5f, a -> Interp.pow3Out.apply(Mathf.slope(a))) * 0.0125f;
            float rise = period(0.5f, 0f, 0.36f, Interp.pow3Out);

            Tmp.v1.trns(i * 45f, stroke);
            Tmp.v2.trns((i + 1f) * 45f, stroke);

            Tmp.v31.set(Tmp.v1.x, structureOffset, Tmp.v1.y);
            Tmp.v32.set(Tmp.v2.x, structureOffset, Tmp.v2.y);
            Tmp.v33.set(Tmp.v32.x, rise * 14.25f * structureScale + structureOffset, Tmp.v32.z);
            Tmp.v34.set(Tmp.v31.x, Tmp.v33.y, Tmp.v31.z);

            normal(nor, Tmp.v33, Tmp.v32, Tmp.v31);
            Tmp.c1.set(monolithMid).a((1f - period(0.5f, 0f, 0.5f, Interp.pow5In)) * rise);
            Tmp.c2.set(Tmp.c1).a(0f);

            Draw3DUtils.quad2(
                batch,
                Tmp.v34, nor, Tmp.c2, beam.u, beam.v,
                Tmp.v33, nor, Tmp.c2, beam.u2, beam.v,
                Tmp.v32, nor, Tmp.c1, beam.u2, beam.v2,
                Tmp.v31, nor, Tmp.c1, beam.u, beam.v2
            );
        }

        for(int i = 0; i < 4; i++){
            float rad = 0.045f;
            float rise = 0.8f + Mathf.sin(period(Interp.linear), 1f / Mathf.PI2, 0.2f);
            float col = 0.25f + period(0.67f, 0f, 0.5f, a -> Interp.smoother.apply(Mathf.slope(a))) * 0.75f;

            Tmp.v1.trns(45f + i * 90f, rad);
            Tmp.v2.trns(45f + (i + 1f) * 90f, rad);

            Tmp.v31.set(Tmp.v1.x, structureOffset, Tmp.v1.y);
            Tmp.v32.set(Tmp.v2.x, structureOffset, Tmp.v2.y);
            Tmp.v33.set(Tmp.v32.x, rise * 5f * structureScale + structureOffset, Tmp.v32.z);
            Tmp.v34.set(Tmp.v31.x, Tmp.v33.y, Tmp.v31.z);

            normal(nor, Tmp.v33, Tmp.v32, Tmp.v31);
            Tmp.c1.set(monolithDark).a(col);
            Tmp.c2.set(Tmp.c1).a(0f);

            Draw3DUtils.quad2(
                batch,
                Tmp.v34, nor, Tmp.c2, shade.u, shade.v,
                Tmp.v33, nor, Tmp.c2, shade.u2, shade.v,
                Tmp.v32, nor, Tmp.c1, shade.u2, shade.v2,
                Tmp.v31, nor, Tmp.c1, shade.u, shade.v2
            );
        }
    }

    @Override
    public void drawActualAtmosphere(Mesh atmosphere, Camera3D cam){
        var shader = CShaders.portalForcefield;
        shader.camera = cam;
        shader.planet = this;

        shader.bind();
        shader.apply();
        atmosphereMesh.render(shader, Gl.triangles);
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
            buffer.resize(graphics.getWidth(), graphics.getHeight());
            buffer.begin(Color.clear);

            var shader = CShaders.celestial;
            shader.light.set(solarSystem.position);
            shader.ambientColor.set(solarSystem.lightColor);
            shader.camPos.set(renderer.planets.cam.position);
            shader.bind();
            shader.apply();

            shader.setUniformMatrix4("u_proj", renderer.planets.cam.combined.val);

            Node base = CModels.portalBase, cage = CModels.portalCage;
            base.localTrns.translation.set(0f, structureOffset, 0f);
            base.localTrns.rotation.idt();
            base.localTrns.scale.set(structureScale, structureScale, structureScale);

            cage.localTrns.translation.set(0f, 5f, 0f);
            cage.localTrns.rotation.set(Vec3.Y, period(Interp.pow5) * 90f);
            cage.localTrns.scale.set(1f, 1f, 1f);
            base.update();

            for(var node : iter(base, cage)){
                shader.setUniformMatrix4("u_trans", mat1.set(transform).mul(node.globalTrns).val);
                shader.setUniformMatrix("u_normal", copyMatrix(mat1, Tmp.m1).inv().transpose());

                node.mesh.containers.each(mesh -> mesh.render(shader));
            }

            for(int i = 0, len = islands.length; i < len; i++){
                var island = islands[i];
                var mesh = islandMeshes[i];

                mat1.set(island.offset, quat.setFromAxis(Vec3.Y, island.rotation));
                mat2.set(transform).mul(mat1);

                mat1.set(v1.set(0f, -Mathf.absin(Time.globalTime, island.hoverScale, island.hoverMag), 0f), quat.idt());
                mat2.mul(mat1);

                shader.setUniformMatrix4("u_trans", mat2.val);
                shader.setUniformMatrix("u_normal", copyMatrix(mat2, Tmp.m1).inv().transpose());
                mesh.render(shader, Gl.triangles);
            }

            buffer.end();

            var blit = CShaders.depthScreenspace;
            blit.buffer = buffer;
            Draw.blit(blit);
        }
    }

    public static class Intersect extends Vec3{
        public @Nullable Ptile intersected;
    }

    private interface IslandBuilder{
        IslandShaper get(int seed, float dstScale, float depth, float depthScale, Color stroke);
    }
}
