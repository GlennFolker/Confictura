package confictura.net;

import arc.func.*;
import arc.struct.*;
import confictura.input.*;
import mindustry.gen.*;
import mindustry.net.*;

import static confictura.ConficturaMod.*;
import static mindustry.Vars.*;

/**
 * Handles various modded client-server synchronizations.
 * @author GlFolker
 */
public final class CCall{
    private CCall(){
        throw new AssertionError();
    }

    public static void init(){
        Net.registerPacket(TapPacket::new);
    }

    /**
     * Client-server synchronization for {@linkplain InputAggregator#tryTap(Player, float, float, Seq) player taps}.
     * This method is called on either clients or self-hosts:
     * <ol>
     *     <li>On clients, send a try-tap packet to servers to retrieve which listeners accepts the tap.</li>
     *     <li>On self-hosts or servers, handle the try-tap packet (sent by either a client on servers, or itself on
     *     self-hosts) and forward the resulting listeners' acceptance of the tap to all connected clients.</li>
     *     <li>Forwarded result packet is received on clients and is handled accordingly, without sending any more packets.</li>
     * </ol>
     * @param player  The player who commited the tap. Retrieved from {@link NetConnection#player} on servers.
     * @param x       X position of the tap.
     * @param y       Y position of the tap.
     * @param targets Which listeners to tap against; may be inspected with {@link InputAggregator#each(Cons)}.
     */
    public static void tap(Player player, float x, float y, Seq<String> targets){
        // If this is a local game, just handle it immediately.
        if(!net.active()){
            var accepted = inputAggregator.tryTap(player, x, y, targets);
            inputAggregator.tap(player, x, y, targets, accepted);
        }else{
            // Otherwise, send a try packet to the server from a client, and result packet from the server to connected clients.
            var packet = new TapPacket();
            packet.x = x;
            packet.y = y;
            packet.targets = targets;

            // If this is a server, handle the request (either from a client on headless servers, or the host itself on
            // self-hosted games), then send the result packet to all connected clients.
            if(net.server()){
                packet.player = player;
                var accepted = packet.results = inputAggregator.tryTap(player, x, y, targets);
                inputAggregator.tap(player, x, y, targets, accepted);
            }

            net.send(packet, true);
        }
    }
}
