package confictura.world.blocks.core;

import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import confictura.content.*;
import confictura.graphics.g2d.*;
import mindustry.graphics.*;
import mindustry.world.blocks.storage.*;

import static arc.Core.*;
import static confictura.graphics.CPal.*;
import static confictura.util.MathUtils.*;
import static mindustry.Vars.*;

public class SatelliteEntry extends CoreBlock{
    public static final float
        zoomStart = 20f, zoomAppear = 16f, zoomFocus = 8f, zoomDone = 6f,
        zoomAppearTime = 45f, zoomFocusTime = 170f, zoomDoneTime = 185f,

        topCurve = 60f, top1Time = 80f, top2Time = 100f, top3Time = 120f;

    static{
        CFx.satelliteEntryTrails.lifetime = zoomDoneTime - zoomAppearTime;
    }

    public TextureRegion[] topRegions;

    public SatelliteEntry(String name){
        super(name);
    }

    @Override
    public void load(){
        super.load();

        topRegions = new TextureRegion[3];
        for(int i = 0; i < 3; i++){
            topRegions[i] = atlas.find(name + "-top-" + (i + 1));
        }
    }

    @Override
    public void drawLanding(CoreBuild build, float x, float y){}

    @Override
    protected void drawLandingThrusters(float x, float y, float rotation, float frame){}

    public class SatelliteEntryBuild extends CoreBuild{
        @Override
        public void beginLaunch(@Nullable CoreBlock launchType){
            if(!headless){
                if(!renderer.isLaunching()){
                    scene.add(new Element(){
                        float time = -1f;

                        {
                            color.set(Color.black);

                            touchable = Touchable.disabled;
                            setFillParent(true);

                            app.post(() -> {
                                time = 0f;
                                actions(Actions.delay(zoomDoneTime / 60f), Actions.remove());
                            });
                        }

                        @Override
                        public void act(float delta){
                            super.act(delta);

                            toFront();
                            ui.loadfrag.toFront();
                            if(state.isMenu()) remove();

                            if(time >= 0f) time += Time.delta;
                        }

                        @Override
                        public void draw(){
                            validate();
                            float time = Math.max(this.time, 0f);

                            float open = Mathf.curve(time, 0f, zoomAppearTime);
                            Draw.color(Color.black, monolithDarker, monolithLighter, Interp.pow10In.apply(open));
                            Draw.alpha(Interp.pow10Out.apply(1f - open));

                            float hw = width / 2f, hh = height / 2f;
                            float cx = x + hw, cy = y + hh;
                            float rad = Interp.pow5In.apply(open) * Math.max(hw, hh);

                            if(rad < hh){
                                Draw2DUtils.crectUi(x, y, width, hh - rad);
                                Draw2DUtils.crectUi(x, height - hh + rad, width, hh - rad);
                            }

                            if(rad < hw){
                                Draw2DUtils.crectUi(x, y + Math.max(hh - rad, 0f), hw - rad, height - Math.max(hh - rad, 0f) * 2f);
                                Draw2DUtils.crectUi(x + hw + rad, y + Math.max(hh - rad, 0f), hw - rad, height - Math.max(hh - rad, 0f) * 2f);
                            }

                            float fade = Mathf.curve(time, 0f, zoomDoneTime);
                            Draw.color(Color.black, Interp.pow2Out.apply(1f - fade) * 0.8f);

                            Draw2DUtils.crectUi(x, y, width, height);
                            Draw.reset();
                        }
                    });

                    Time.run(zoomAppearTime + 30f, () -> CFx.satelliteEntrySignal.at(this));
                    Time.run(zoomAppearTime, () -> CFx.satelliteEntryTrails.at(this));
                }
            }
        }

        @Override
        public void endLaunch(){
            if(!renderer.isLaunching()){
                renderer.setScale(Scl.scl(zoomDone - 1f));

                ui.hudfrag.shown = true;
                ui.hudGroup.actions(Actions.fadeIn(0.1f));

                for(int i = 0; i < 4; i++){
                    float angle = 90f * i;
                    Tmp.v1.trns(angle, size * tilesize / 2f - 4f).add(this);
                    CFx.satelliteEntryExhaust.at(Tmp.v1.x, Tmp.v1.y, angle);
                }
            }
        }

        @Override
        public float landDuration(){
            return renderer.isLaunching() ? 0f : zoomDoneTime;
        }

        @Override
        public Music landMusic(){
            //TODO replace dummy track with them juicy GlennFolker OST
            return new Music();
        }

        @Override
        public float zoomLaunching(){
            camera.position.set(this);
            ui.hudfrag.shown = false;
            ui.hudGroup.color.a = 0f;

            if(renderer.isLaunching()){
                //TODO
                return Scl.scl(4f);
            }else{
                float in = renderer.getLandTimeIn() * landDuration();
                return
                    bound(in, 0f, zoomAppearTime + 30f, Scl.scl(zoomStart), Scl.scl(zoomAppear), Interp.pow2Out) +
                    bound(in, zoomAppearTime + 30f, zoomFocusTime, Scl.scl(zoomAppear), Scl.scl(zoomFocus), Interp.pow2) +
                    bound(in, zoomFocusTime, zoomDoneTime, Scl.scl(zoomFocus), Scl.scl(zoomDone), Interp.pow3In);
            }
        }

        @Override
        public void draw(){
            float z = Draw.z();
            Draw.rect(region, x, y);

            if(renderer.getLandTime() > 0f){
                float in = renderer.getLandTimeIn() * landDuration();
                if(!renderer.isLaunching()){
                    Draw.z(Layer.blockAdditive);
                    Draw.blend(Blending.additive);

                    Draw.color(monolithDarker, curve(in, top1Time, top1Time + topCurve, 0f, 1f, Interp.pow2));
                    Draw.rect(topRegions[0], x, y);

                    Draw.color(monolithMid, curve(in, top2Time, top2Time + topCurve, 0f, 1f, Interp.pow2));
                    Draw.rect(topRegions[1], x, y);

                    Draw.color(monolithLighter, curve(in, top3Time, top3Time + topCurve, 0f, 1f, Interp.pow2));
                    Draw.rect(topRegions[2], x, y);

                    Draw.blend();
                }
            }

            Draw.reset();
            Draw.z(z);
        }

        @Override
        public void drawLanding(CoreBlock block){}

        @Override
        public void updateLandParticles(){}
    }
}
