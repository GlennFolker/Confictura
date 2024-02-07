package confictura.graphics.g3d.mesh;

import arc.graphics.*;
import arc.math.geom.*;
import confictura.graphics.g3d.MeshDrawer.*;

public class CelestialVertex extends Vertex{
    public Vec3 position = new Vec3();
    public Vec3 normal = new Vec3();
    public Color color = new Color();

    public CelestialVertex(Vec3 position, Vec3 normal, Color color){
        this.position.set(position);
        this.normal.set(normal);
        this.color.set(color);
    }

    @Override
    public float[] asFloats(){
        return new float[]{
            position.x, position.y, position.z,
            normal.x, normal.y, normal.z,
            color.toFloatBits()
        };
    }

    @Override
    public VertexAttribute[] attributes(){
        return new VertexAttribute[]{VertexAttribute.position3, VertexAttribute.normal, VertexAttribute.color};
    }

    public static class CelestialRect extends Face<CelestialVertex>{
        public float x, y, z, yaw, pitch, roll, width, height;
        public Color color = new Color();

        public CelestialRect(float x, float y, float z, float yaw, float pitch, float roll, float width, float height, Color color){
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.roll = roll;
            this.width = width;
            this.height = height;
            this.color.set(color);
        }

        @Override
        public CelestialVertex[] vertices(){
            var rot = new Quat().setEulerAngles(yaw, pitch, roll);
            var nor = rot.transform(new Vec3(Vec3.X));

            float hw = width / 2f, hh = height / 2f;
            return new CelestialVertex[]{
                new CelestialVertex(rot.transform(new Vec3(0f, -hh, -hw)), nor, color),
                new CelestialVertex(rot.transform(new Vec3(0f, -hh, hw)), nor, color),
                new CelestialVertex(rot.transform(new Vec3(0f, hh, hw)), nor, color),
                new CelestialVertex(rot.transform(new Vec3(0f, hh, -hw)), nor, color)
            };
        }

        @Override
        public short[] indices(){
            return new short[]{0, 1, 2, 2, 3, 0};
        }
    }
}
