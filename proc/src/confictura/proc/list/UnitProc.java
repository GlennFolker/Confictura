package confictura.proc.list;

import arc.files.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.noise.*;
import confictura.graphics.g2d.*;
import confictura.proc.*;
import confictura.proc.GenAtlas.*;
import mindustry.gen.*;
import mindustry.type.*;

import static confictura.ConficturaMod.*;
import static confictura.proc.ConficturaProc.*;
import static mindustry.Vars.*;

public class UnitProc implements Proc{
    @Override
    public void init(Cons<Runnable> async){
        var packer = new GenPacker();
        content.units().each(u -> isConfictura(u) && u.generateIcons, u -> async.get(() -> {
            var name = u.name.substring("confictura-".length());
            try{
                u.init();
                u.loadIcon();
                u.load();

                if(u.previewRegion == u.region){
                    var r = atlas.conv(u.region);
                    var prev = new GenRegion("confictura-" + name + "-preview", r.file.sibling(name + "-preview.png"), r.pixmap().copy());
                    prev.save(true);

                    u.previewRegion = prev;
                }

                u.createIcons(packer);
                u.load();

                var e = u.constructor.get();
                Func<Pixmap, Pixmap> outline = pix -> pix.outline(u.outlineColor, u.outlineRadius);
                Cons2<Pixmap, Cons<Pixmap>> outlineThen = (pix, cons) -> {
                    var out = outline.get(pix);
                    cons.get(out);
                    out.dispose();
                };

                // Don't account for `Crawlc` because the mod won't have any of those units anyway.
                var image = outline.get(atlas.conv(u.previewRegion).pixmap());
                Func<Weapon, Pixmap> weaponRegion = w -> atlas.find(w.name + "-preview", w.region).pixmap();
                Cons2<Weapon, Pixmap> drawWeapon = (w, pixmap) -> {
                    if(w.flipSprite) pixmap = pixmap.flipX();
                    image.draw(
                        pixmap,
                        (int)(w.x / Draw.scl + image.width / 2f - w.region.width / 2f),
                        (int)(-w.y / Draw.scl + image.height / 2f - w.region.height / 2f),
                        true
                    );

                    if(w.flipSprite) pixmap.dispose();
                };

                boolean anyUnder = false;
                for(var w : u.weapons){
                    if(!w.region.found() || w.layerOffset >= 0f) continue;
                    outlineThen.get(weaponRegion.get(w), pix -> drawWeapon.get(w, pix));
                    anyUnder = true;
                }

                if(anyUnder) outlineThen.get(atlas.conv(u.previewRegion).pixmap(), pix -> image.draw(pix, true));
                if(e instanceof Tankc){
                    outlineThen.get(atlas.conv(u.treadRegion).pixmap(), pix -> {
                        image.draw(pix, image.width / 2 - pix.width / 2, image.height / 2 - pix.height / 2, true);
                        image.draw(atlas.conv(u.previewRegion).pixmap(), true);
                    });
                }

                if(e instanceof Mechc){
                    CPixmaps.drawCenter(image, atlas.conv(u.baseRegion).pixmap());

                    var leg = atlas.conv(u.legRegion).pixmap();
                    CPixmaps.drawCenter(image, leg);
                    CPixmaps.drawCenter(image, leg = leg.flipX());
                    leg.dispose();

                    image.draw(atlas.conv(u.previewRegion).pixmap(), true);
                }

                for(var w : u.weapons){
                    if(!w.region.found() || w.layerOffset < 0f) continue;
                    outlineThen.get(weaponRegion.get(w), pix -> drawWeapon.get(w, pix));
                }

                if(u.drawCell){
                    image.draw(atlas.conv(u.previewRegion).pixmap(), true);

                    var cell = atlas.conv(u.cellRegion).pixmap().copy();
                    cell.replace(in -> switch(in){
                        case 0xffffffff -> 0xffa664ff;
                        case 0xdcc6c6ff, 0xdcc5c5ff -> 0xd06b53ff;
                        default -> 0;
                    });

                    CPixmaps.drawCenter(image, cell);
                    cell.dispose();
                }

                for(var w : u.weapons){
                    if(!w.region.found() || w.layerOffset < 0f) continue;

                    var pix = weaponRegion.get(w);
                    if(w.top) pix = outline.get(pix);

                    drawWeapon.get(w, pix);
                    if(w.cellRegion.found()){
                        var cell = atlas.conv(w.cellRegion).pixmap().copy();
                        cell.replace(in -> switch(in){
                            case 0xffffffff -> 0xffa664ff;
                            case 0xdcc6c6ff, 0xdcc5c5ff -> 0xd06b53ff;
                            default -> 0;
                        });

                        drawWeapon.get(w, cell);
                        cell.dispose();
                    }
                }

                new GenRegion("confictura-" + name + "-full", atlas.conv(u.previewRegion).file.sibling(name + "-full.png"), image).save(true);

                var rand = new Rand();
                rand.setSeed(name.hashCode());

                int splits = 3;
                float degrees = rand.random(360f);
                float offsetRange = Math.max(image.width, image.height) * 0.15f;
                var offset = new Vec2(1f, 1f).rotate(rand.random(360f)).setLength(rand.random(0f, offsetRange)).add(image.width / 2f, image.height / 2f);

                var wrecks = new Pixmap[splits];
                for(int i = 0; i < wrecks.length; i++){
                    wrecks[i] = new Pixmap(image.width, image.height);
                }

                var noise = new VoronoiNoise(u.id, true);
                image.each((x, y) -> {
                    boolean ridged = Math.max(Ridged.noise2d(1, x, y, 3, 1f / (20f + image.width/8f)), 0) > 0.16f;
                    if(noise.noise(x, y, 1f / (14f + image.width / 40f)) <= 0.47){
                        float dst =  offset.dst(x, y);
                        float dev = (float)Noise.rawNoise(dst / (9f + image.width/70f)) * (60 + image.width/30f);

                        int section = (int)Mathf.clamp(Mathf.mod(offset.angleTo(x, y) + dev + degrees, 360f) / 360f * splits, 0, splits - 1);
                        wrecks[section].setRaw(x, y, Color.muli(image.getRaw(x, y), ridged ? 0.7f : 1f));
                    }
                });

                for(int i = 0; i < wrecks.length; i++){
                    new GenRegion("confictura-" + name + "-wreck" + i, Fi.get("sprites").child("rubble").child(u.name + "-wreck" + i), wrecks[i]).save(true);
                }

                int size = Math.min(Math.max(image.width, image.height), 128);
                var rect = Scaling.fit.apply(image.width, image.height, size, size);
                new GenRegion(
                    "unit-confictura-" + name + "-ui",
                    Fi.get("sprites").child("ui").child("unit-confictura-" + name + "-ui.png"),
                    CPixmaps.copyScaled(image, (int)rect.x, (int)rect.y)
                ).save(true);
            }catch(Throwable t){
                Log.warn("Skipping '@': @", name, Strings.getStackTrace(Strings.getFinalCause(t)));
            }
        }));
    }

    @Override
    public void finish(){}
}
