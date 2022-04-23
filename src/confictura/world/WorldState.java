package confictura.world;

import arc.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import arc.util.serialization.*;
import confictura.assets.*;
import confictura.assets.CShaders.CollapseShader.*;
import confictura.util.*;
import confictura.world.blocks.environment.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.core.GameState.*;
import mindustry.game.EventType.*;
import mindustry.io.*;
import mindustry.world.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import static mindustry.Vars.*;

public final class WorldState{
    public static final String dataKey = "confictura-world-state-save-data";

    public static final IntGrid collapseGrid = new IntGrid(0, 0);
    public static final Seq<CollapseData> collapses = new Seq<>();

    private static boolean bound;
    private static final ReusableByteOutStream out = new ReusableByteOutStream();
    private static final ReusableByteInStream in = new ReusableByteInStream();
    private static final Writes write = new Writes(null);
    private static final Reads read = new Reads(null);

    private WorldState(){
        throw new AssertionError();
    }

    public static void configure(){
        Events.on(ClientLoadEvent.class, e -> Events.run(Trigger.update, WorldState::update));
    }

    public static void init(){
        collapses.clear();

        int collapseMax = CollapseFloor.max();
        int[] pos = new int[collapseMax * 2], extents = new int[collapseMax * 2];
        boolean[] found = new boolean[collapseMax];

        Arrays.fill(pos, Integer.MAX_VALUE);
        collapseGrid.each((x, y, t) -> {
            if(t < 0 || t >= collapseMax) return;
            found[t] = true;

            int i = t * 2;
            pos[i] = Math.min(pos[i], x);
            pos[i + 1] = Math.min(pos[i + 1], y);
            extents[i] = Math.max(extents[i], x);
            extents[i + 1] = Math.max(extents[i + 1], y);
        });

        for(int i = 0; i < collapseMax; i++){
            if(!found[i]) continue;

            int s = i * 2;
            if(extents[s] - pos[s] + 1 > 64 || extents[s + 1] - pos[s + 1] + 1 > 64){
                throw new IllegalStateException("Collapse boundary must not exceed 64*64.");
            }
        }

        for(int i = 0; i < collapseMax; i++){
            if(!found[i]){
                collapses.add(new CollapseData(i));
            }else{
                int s = i * 2;
                float
                    tx = pos[s] * 8f, ty = pos[s + 1] * 8f,
                    ex = extents[s] * 8f + 8f, ey = extents[s + 1] * 8f + 8f;

                collapses.add(new CollapseData(i, tx, ty, ex - tx, ey - ty));
            }
        }

        if(!headless) CShaders.collapse.reset();
    }

    public static void update(){
        if(!bound) return;
        for(CollapseData data : collapses){
            if(data.triggered && !data.handled) data.handle();
        }
    }

    public static StringMap write(IntGrid collapseGrid){
        StringMap data = new StringMap();

        out.reset();
        try(DataOutputStream stream = new DataOutputStream(new DeflaterOutputStream(out))){
            write.output = stream;
            writeCollapses(write, collapseGrid);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
        data.put("collapses", new String(Base64Coder.encode(out.getBytes())));

        write.output = null;
        return data;
    }

    public static void writeCollapses(Writes write, IntGrid grid){
        write.b(0);
        write.s(grid.width);
        write.s(grid.height);
        for(int item : grid.items) write.b(item);
    }

    public static void read(StringMap data, IntGrid collapseGrid){
        String collapses = data.get("collapses");
        if(collapses != null){
            in.setBytes(Base64Coder.decode(collapses));
            try(DataInputStream stream = new DataInputStream(new InflaterInputStream(in))){
                read.input = stream;
                readCollapses(read, collapseGrid);
            }catch(IOException e){
                throw new RuntimeException(e);
            }
        }else{
            collapseGrid.resize(world.width(), world.height());
            collapseGrid.fill(-1);
        }

        read.input = null;
    }

    public static void readCollapses(Reads read, IntGrid grid){
        byte version = read.b();
        if(version != 0) throw new IllegalStateException("Unknown version: " + version);

        int w = read.s(), h = read.s();
        grid.resize(w, h);

        int len = w * h;
        for(int i = 0; i < len; i++) grid.items[i] = read.b();
    }

    public static class CollapseData{
        public final Rect bound = new Rect();
        public final int index;
        public float timestamp = -1f, duration = 60f;

        private boolean triggered, handled;

        public CollapseData(int index){
            this(index, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
        }

        public CollapseData(int index, float x, float y, float w, float h){
            this.index = index;
            bound.set(x, y, w, h);
        }

        public boolean valid(){
            return
                !Float.isNaN(bound.x) && !Float.isNaN(bound.y) &&
                !Float.isNaN(bound.width) && !Float.isNaN(bound.height);
        }

        public void trigger(){
            if(triggered) return;
            triggered = true;

            if(!headless){
                CollapseDraw draw = Structs.find(CShaders.collapse.draws, e -> e.data == this);
                if(draw != null) draw.prepareCapture();
            }
        }

        public void handle(){
            if(handled) return;
            handled = true;

            int tx = World.toTile(bound.x), ty = World.toTile(bound.y),
                ex = tx + World.toTile(bound.width), ey = ty + World.toTile(bound.height);

            CollapseFloor floor = CollapseFloor.get(index);
            for(int x = tx; x < ex; x++){
                for(int y = ty; y < ey; y++){
                    if(!world.tiles.in(x, y) || collapseGrid.get(x, y) != index) continue;

                    Tile tile = world.rawTile(x, y);
                    tile.setBlock(Blocks.air);
                    tile.setOverlay(Blocks.air);
                    tile.setFloor(floor);
                }
            }
        }
    }
}
