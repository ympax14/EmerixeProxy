package com.emerixe.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.emerixe.MinecraftProxy;
import com.emerixe.packet.MinecraftPacket;
import com.emerixe.packet.packets.LoginStartPacket;
import com.emerixe.packet.packets.ServerTransferPacket;
import com.emerixe.packet.registry.PacketRegistry;
import com.emerixe.router.ServerRouter;
import com.emerixe.session.PlayerSession;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ProxyMessageHandler extends ChannelInboundHandlerAdapter {
    private final PacketRegistry packetRegistry = new PacketRegistry();
    private final Map<UUID, PlayerSession> playerSessions = new HashMap<>();

    public ProxyMessageHandler() {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Intercepter le paquet "Login Start" envoyé par le client
        System.out.println("[PROXY] Nouveau joueur connecté");

        MinecraftProxy.getInstance().getPlayerConnectionManager().connectToServer(ctx.channel(), "hub");

        // Pour intercepter les paquets, d'abord traiter les paquets qui arrivent
        // via la méthode channelRead, ici on est dans channelActive donc on ne peut pas
        // encore récupérer directement les paquets, c'est pourquoi nous devons laisser
        // le framework recevoir le paquet avant de le traiter.

        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            ByteBuf copiedBuf = buf.copy();
    
            try {
                int packetId = MinecraftPacket.readVarInt(buf); // Lire l'ID du paquet

                /*if (packetId == 15) {
                    //ctx.channel().writeAndFlush(msg);
                    // Rediriger le joueur vers le HUB
                    //MinecraftProxy.getInstance().getPlayerConnectionManager().connectToServer(ctx.channel(), "hub");
                    //relayPacketToHub(copiedBuf);
                } else {*/
                    MinecraftPacket packet = packetRegistry.decodePacket(packetId, buf);
                    if (packet != null) {
                        handlePacket(packet);
                    }
                //}
            } finally {
                copiedBuf.release();
            }
        }
        super.channelRead(ctx, msg);
    }

    private void handlePacket(MinecraftPacket packet) {
        if (packet instanceof LoginStartPacket) {
            LoginStartPacket loginPacket = (LoginStartPacket) packet;

            String targetServer = loginPacket.getOriginServer();  // Exemple : "game1" ou "hub"

            // Récupérer la session du joueur et mettre à jour son serveur
            UUID playerUUID = loginPacket.getPlayerUUID();
            PlayerSession session = playerSessions.get(playerUUID);
            if (session != null) {
                session.setOrigin(targetServer);  // Met à jour le serveur cible
            }

            System.out.println("Login Start : " + loginPacket.getPlayerName());
        } else if (packet instanceof ServerTransferPacket) {
            ServerTransferPacket transferPacket = (ServerTransferPacket) packet;

            String fromServer;
            String targetServer = transferPacket.getTargetServer();  // Exemple : "game1" ou "hub"

            // Récupérer la session du joueur et mettre à jour son serveur
            UUID playerUUID = transferPacket.getPlayerUUID();
            PlayerSession session = playerSessions.get(playerUUID);

            if (session == null) return;
            
            fromServer = session.getOrigin();
            session.setOrigin(targetServer);  // Met à jour le serveur cible
            

            System.out.println("Le joueur a été transféré du serveur " + fromServer + " vers le serveur " + targetServer);
        }
        // Ajouter des cas pour d'autres paquets
    }

    private void relayPacketToHub(ByteBuf packet) {
        ServerRouter serverRouter = MinecraftProxy.getInstance().getServerRouter();
        Channel hubChannel = serverRouter.openServerChannel(serverRouter.getServer("hub")); // Obtenez le canal vers le hub
    
        if (hubChannel != null && hubChannel.isActive()) {
            hubChannel.writeAndFlush(packet.retain());  // Relayer le paquet au serveur hub
            System.out.println("Paquet ID 15 relayé directement au hub depuis le canal client.");
        } else {
            System.err.println("Le canal vers le hub est inactif ou inexistant. Impossible de relayer le paquet.");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}