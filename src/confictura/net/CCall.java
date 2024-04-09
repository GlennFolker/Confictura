package confictura.net;

import mindustry.gen.*;
import mindustry.net.*;

import static confictura.ConficturaMod.*;
import static mindustry.Vars.*;

public final class CCall{
    private CCall(){
        throw new AssertionError();
    }

    public static void init(){
        Net.registerPacket(TapPacket::new);
    }

    /** Called on clients, handled immediately on said client, and sent to the server to be handled and broadcast to other clients. */
    public static void tap(Player player, float x, float y){
        inputAggregator.tap(player, x, y);
        tapSend(player, x, y, false);
    }

    static void tapSend(Player player, float x, float y, boolean forwarded){
        if(net.server() || net.client()){
            var packet = new TapPacket();
            if(net.server()) packet.player = player;
            packet.x = x;
            packet.y = y;

            if(forwarded){
                net.sendExcept(player.con, packet, false);
            }else{
                net.send(packet, false);
            }
        }
    }
}
