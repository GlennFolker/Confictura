package confictura.net;

import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import confictura.input.InputAggregator.*;
import confictura.io.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.net.*;

import static confictura.ConficturaMod.*;
import static mindustry.Vars.*;

public class TapPacket extends Packet{
    public @Nullable Player player;
    public float x, y;

    public Seq<String> targets;
    public @Nullable Seq<TapResult> results;

    @Override
    public void write(Writes write){
        if(net.server()){
            TypeIO.writeEntity(write, player);
            CTypeIO.writeTaps(write, results);
        }

        write.f(x);
        write.f(y);
        CTypeIO.writeStrings(write, targets);
    }

    @Override
    public void read(Reads read, int length){
        BAIS.setBytes(read.b(length), 0, length);
    }

    @Override
    public void handled(){
        var read = READ;
        if(net.client()){
            player = TypeIO.readEntity(read);
            results = CTypeIO.readTaps(read);
        }

        x = read.f();
        y = read.f();
        targets = CTypeIO.readStrings(read);
    }

    @Override
    public void handleServer(NetConnection con){
        // On servers, handle the try packet sent from a client and send the result packet to all connected clients.
        if(con.player == null || con.kicked) return;
        CCall.tap(con.player, x, y, targets);
    }

    @Override
    public void handleClient(){
        // On clients, handle the result packet from the server and don't send anything else.
        inputAggregator.tap(player, x, y, targets, results);
    }
}
