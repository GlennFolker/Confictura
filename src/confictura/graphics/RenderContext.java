package confictura.graphics;

import arc.*;
import arc.graphics.g3d.*;
import arc.math.*;
import mindustry.game.EventType.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class RenderContext{
    public float fovX = 60f;
    public Camera3D cam = new Camera3D(){{
        near = 1f;

        up.set(0f, 0f, -1f);
        direction.set(0f, -1f, 0f);
    }};

    protected float[] darkness;

    public RenderContext(){
        Events.run(Trigger.draw, () -> {
            cam.resize(camera.width, camera.height);

            double y = cam.width / 2d / Math.tan(fovX / 2d * Mathf.doubleDegRad);
            cam.position.set(camera.position.x, (float)y, -camera.position.y);
            cam.fov = (float)(2d * Math.atan2(cam.height, 2d * y) * Mathf.doubleRadDeg);
            cam.far = Math.max(150f, cam.position.y * 1.5f);
            cam.update();
        });

        Events.on(WorldLoadEvent.class, e -> {
            darkness = new float[world.width() * world.height()];
            world.tiles.each(this::updateDarkness);
        });

        Events.on(TileChangeEvent.class, e -> updateDarkness(e.tile.x, e.tile.y));
    }

    protected void updateDarkness(int x, int y){
        float dark = world.getDarkness(x, y);
        if(dark > 0f){
            darkness[world.packArray(x, y)] = 1f - Math.min((dark + 0.5f) / 4f, 1f);
        }else{
            darkness[world.packArray(x, y)] = 1f;
        }
    }

    public float darkness(float x, float y){
        x /= tilesize;
        y /= tilesize;

        int x1 = (int)x, x2 = x1 + 1,
            y1 = (int)y, y2 = y1 + 1;

        float out = state.rules.borderDarkness ? 0f : 1f;
        var t = world.tiles;

        return Mathf.lerp(
            Mathf.lerp(t.in(x1, y1) ? darkness[world.packArray(x1, y1)] : out, t.in(x2, y1) ? darkness[world.packArray(x2, y1)] : out, x % 1f),
            Mathf.lerp(t.in(x1, y2) ? darkness[world.packArray(x1, y2)] : out, t.in(x2, y2) ? darkness[world.packArray(x2, y2)] : out, x % 1f),
            y % 1f
        );
    }
}
