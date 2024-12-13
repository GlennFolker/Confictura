package confictura.world.celestial;

import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.graphics.g3d.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import arc.util.*;
import confictura.graphics.*;
import confictura.graphics.gl.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;

import static arc.Core.*;
import static mindustry.Vars.*;

/**
 * Just a regular planet, but with a fixed atmosphere shader at the little cost of performance.
 * @author GlFolker
 */
public class AtmospherePlanet extends Planet{
    public @Nullable CFrameBuffer buffer;

    public AtmospherePlanet(String name, Planet parent, float radius){
        super(name, parent, radius);
    }

    public AtmospherePlanet(String name, Planet parent, float radius, int sectorSize){
        super(name, parent, radius, sectorSize);
    }

    @Override
    public void load(){
        super.load();
        if(!headless && buffer == null){
            buffer = new CFrameBuffer(2, 2, true);
            buffer.getTexture().setFilter(TextureFilter.nearest);
        }
    }

    @Override
    public void drawAtmosphere(Mesh atmosphere, Camera3D cam){
        Gl.depthMask(false);
        Blending.additive.apply();

        var shader = CShaders.depthAtmosphere;
        shader.camera = cam;
        shader.planet = this;
        shader.bind();
        shader.apply();
        atmosphere.render(shader, Gl.triangles);

        Blending.normal.apply();
        Gl.depthMask(true);
    }

    public class AtmosphereHexMesh implements GenericMesh{
        protected Mesh mesh;

        public AtmosphereHexMesh(HexMesher mesher, int divisions){
            mesh = MeshBuilder.buildHex(mesher, divisions, false, radius, 0.2f);
        }

        public AtmosphereHexMesh(int divisions){
            this(generator, divisions);
        }

        @Override
        public void render(PlanetParams params, Mat3D projection, Mat3D transform){
            buffer.resize(graphics.getWidth(), graphics.getHeight());
            buffer.begin(Color.clear);

            var shader = Shaders.planet;
            shader.planet = AtmospherePlanet.this;
            shader.lightDir.set(solarSystem.position).sub(position).rotate(Vec3.Y, getRotation()).nor();
            shader.ambientColor.set(solarSystem.lightColor);
            shader.bind();
            shader.setUniformMatrix4("u_proj", renderer.planets.cam.combined.val);
            shader.setUniformMatrix4("u_trans", transform.val);
            shader.apply();
            mesh.render(shader, Gl.triangles);

            buffer.end();

            var blit = CShaders.depthScreenspace;
            blit.buffer = buffer;
            Draw.blit(blit);
        }
    }
}
