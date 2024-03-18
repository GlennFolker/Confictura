package confictura.proc;

import arc.files.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import confictura.graphics.g2d.*;
import confictura.proc.GenAtlas.*;
import mindustry.graphics.*;

import static confictura.proc.ConficturaProc.*;

public class GenPacker extends MultiPacker implements FreeablePacker{
    @Override
    public PixmapRegion get(String name){
        return new PixmapRegion(atlas.find(name).pixmap());
    }

    @Override
    public void delete(String name){
        atlas.delete(name);
    }

    @Override
    public void delete(PageType type, String name){
        atlas.delete(name);
    }

    @Override
    public boolean has(String name){
        return atlas.has(name);
    }

    @Override
    public boolean has(PageType type, String name){
        return atlas.has(name);
    }

    @Override
    public void add(PageType type, String name, PixmapRegion region, int[] splits, int[] pads){
        if(name == null) throw new IllegalArgumentException("Name must not be null.");
        boolean prefixed = false;

        int index = name.indexOf('-');
        if(!name.startsWith("confictura-") && !(prefixed = (index == -1 || name.substring(index + 1).startsWith("confictura-")))) return;

        var reg = new GenRegion(name, Fi.get("sprites").child(switch(type){
            case main -> "icons";
            case environment -> "blocks/environment";
            case ui -> "ui";
            case rubble -> "rubble";
            case editor -> "editor";
        }).child((prefixed ? name : name.substring("confictura-".length())) + ".png"), region.crop());
        reg.splits = splits;
        reg.pads = pads;
        reg.save(true);
    }

    @Override
    public void dispose(){}

    @Override
    public TextureAtlas flush(TextureFilter filter, TextureAtlas atlas){
        throw new AssertionError("Stub!");
    }

    @Override
    public PixmapPacker getPacker(PageType type){
        throw new AssertionError("Stub!");
    }

    @Override
    public void printStats(){}
}
