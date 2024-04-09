package confictura.net;

import arc.util.io.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.net.*;

import static confictura.ConficturaMod.*;
import static mindustry.Vars.*;

public class TapPacket extends Packet{
    /** {@code null} when sent from {@linkplain Net#client() clients}, as it is obtained from the sender's connection. */
    public Player player;
    public float x, y;

    public TapPacket(){}

    @Override
    public void write(Writes write){
        if(net.server()) TypeIO.writeEntity(write, player);
        write.f(x);
        write.f(y);
    }

    @Override
    public void read(Reads read){
        if(net.client()) player = TypeIO.readEntity(read);
        x = read.f();
        y = read.f();
    }

    @Override
    public void handleServer(NetConnection con){
        if(con.player == null || con.kicked) return;

        inputAggregator.tap(con.player, x, y);
        CCall.tapSend(con.player, x, y, true);
    }

    @Override
    public void handleClient(){
        inputAggregator.tap(player, x, y);
    }
}
