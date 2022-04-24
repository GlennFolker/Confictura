package confictura.graphics;

import arc.*;
import confictura.assets.*;
import mindustry.graphics.*;

import static mindustry.Vars.*;

public class CCacheLayer{
    public static CollapseLayer collapse;

    public static void load(){
        CacheLayer.add(collapse = new CollapseLayer());
    }

    public static class CollapseLayer extends CacheLayer{
        public boolean stenciling;

        @Override
        public void begin(){
            renderer.blocks.floor.endc();
            renderer.effectBuffer.begin();

            Core.graphics.clear(0f, 0f, 0f, 0f);
            renderer.blocks.floor.beginc();
        }

        @Override
        public void end(){
            renderer.blocks.floor.endc();
            renderer.effectBuffer.end();

            renderer.effectBuffer.blit(stenciling ? Shaders.screenspace : CShaders.collapse);
            renderer.blocks.floor.beginc();
        }
    }
}
