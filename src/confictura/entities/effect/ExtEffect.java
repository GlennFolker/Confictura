package confictura.entities.effect;

import arc.func.*;
import arc.graphics.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.gen.*;

public class ExtEffect extends CEffect{
    public Prov<? extends EffectState> constructor;
    public @Nullable Prov<Object> data;

    public ExtEffect(float lifetime, Cons<EffectContainer> e){
        this(EffectState::create, lifetime, e);
    }

    public ExtEffect(float lifetime, float clip, Cons<EffectContainer> e){
        this(EffectState::create, lifetime, clip, e);
    }

    public ExtEffect(Prov<? extends EffectState> constructor, float lifetime, Cons<EffectContainer> e){
        this(constructor, null, lifetime, e);
    }

    public ExtEffect(Prov<? extends EffectState> constructor, float lifetime, float clip, Cons<EffectContainer> e){
        this(constructor, null, lifetime, clip, e);
    }

    public ExtEffect(Prov<? extends EffectState> constructor, @Nullable Prov<Object> data, float lifetime, Cons<EffectContainer> e){
        this(constructor, data, lifetime, 50f, e);
    }

    public ExtEffect(Prov<? extends EffectState> constructor, @Nullable Prov<Object> data, float lifetime, float clip, Cons<EffectContainer> e){
        super(lifetime, clip, e);
        this.constructor = constructor;
        this.data = data;
    }

    @Override
    protected void add(float x, float y, float rotation, Color color, Object data){
        inst(x, y, rotation, color, data).add();
    }

    protected EffectState inst(float x, float y, float rotation, Color color, Object data){
        var e = constructor.get();
        e.effect = this;
        e.rotation = baseRotation + rotation;
        e.data = this.data != null ? this.data.get() : data;
        e.lifetime = lifetime;
        e.set(x, y);
        e.color.set(color);
        if(followParent && data instanceof Posc p){
            e.parent = p;
            e.rotWithParent = rotWithParent;
        }

        return e;
    }
}
