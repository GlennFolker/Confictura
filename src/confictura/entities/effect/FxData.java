package confictura.entities.effect;

import arc.math.geom.*;
import arc.util.io.*;
import confictura.gen.*;
import ent.anno.Annotations.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;

@EntityPoint
public class FxData implements Posc, Rotc{
    public Posc delegate;
    public Object data;

    public int id = EntityGroup.nextId();

    protected FxData(){}

    public static FxData create(){
        return new FxData();
    }

    @Override
    public Floor floorOn(){
        return delegate.floorOn();
    }

    @Override
    public Building buildOn(){
        return delegate.buildOn();
    }

    @Override
    public boolean onSolid(){
        return delegate.onSolid();
    }

    @Override
    public float getX(){
        return delegate.getX();
    }

    @Override
    public float getY(){
        return delegate.getY();
    }

    @Override
    public float x(){
        return delegate.getX();
    }

    @Override
    public float y(){
        return delegate.getY();
    }

    @Override
    public float rotation(){
        return delegate instanceof Rotc rot ? rot.rotation() : 0f;
    }

    @Override
    public int tileX(){
        return delegate.tileX();
    }

    @Override
    public int tileY(){
        return delegate.tileY();
    }

    @Override
    public Block blockOn(){
        return delegate.blockOn();
    }

    @Override
    public Tile tileOn(){
        return delegate.tileOn();
    }

    // These setters do nothing; why would anybody want to modify state from effects?
    @Override
    public void set(Position position){}

    @Override
    public void set(float x, float y){}

    @Override
    public void trns(Position position){}

    @Override
    public void trns(float x, float y){}

    @Override
    public void x(float x){}

    @Override
    public void y(float y){}

    @Override
    public void rotation(float rotation){}

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entityc> T self(){
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T as(){
        return (T)this;
    }

    @Override
    public boolean isAdded(){
        return delegate.isAdded();
    }

    @Override
    public boolean isLocal(){
        return delegate.isLocal();
    }

    @Override
    public boolean isNull(){
        return delegate.isNull();
    }

    @Override
    public boolean isRemote(){
        return delegate.isRemote();
    }

    @Override
    public boolean serialize(){
        return false;
    }

    @Override
    public int classId(){
        return EntityRegistry.getID(FxData.class);
    }

    @Override
    public int id(){
        return id;
    }

    @Override
    public void id(int id){
        this.id = id;
    }

    @Override
    public void add(){}

    @Override
    public void remove(){}

    @Override
    public void update(){}

    @Override
    public void write(Writes writes){}

    @Override
    public void read(Reads reads){}

    @Override
    public void afterRead(){}

    @Override
    public void afterAllRead(){}
}
