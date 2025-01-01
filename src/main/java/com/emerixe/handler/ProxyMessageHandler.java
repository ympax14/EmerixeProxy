package com.emerixe.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.emerixe.MinecraftProxy;
import com.emerixe.packet.MinecraftPacket;
import com.emerixe.packet.packets.LoginStartPacket;
import com.emerixe.packet.packets.ServerTransferPacket;
import com.emerixe.packet.registry.PacketRegistry;
import com.emerixe.session.PlayerSession;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ProxyMessageHandler extends ChannelInboundHandlerAdapter {
    private final PacketRegistry packetRegistry = new PacketRegistry();
    private final Map<UUID, PlayerSession> playerSessions = new HashMap<>();

    public ProxyMessageHandler() {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
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
            ByteBuf copiedBuf2 = buf.copy();
    
            try {
                int packetId = MinecraftPacket.readVarInt(copiedBuf); // Lire l'ID du paquet

                // Intercepter le paquet "LoginStart" envoyé par le client
                if (packetId == 15) {
                    if (!MinecraftProxy.getInstance().getPlayerConnectionManager().playerIsAlreadyConnected(ctx.channel().remoteAddress())) {
                        System.out.println("[PROXY] Nouveau joueur (" + ctx.channel().remoteAddress() + ") connecté, redirection au HUB.");
                    }

                    MinecraftProxy.getInstance().getPlayerConnectionManager().connectToServer(
                        "hub",
                        ctx.channel(),
                        channel -> {
                            channel.writeAndFlush(copiedBuf2);
                        }, null);
                } else {
                    buf.retain();
                    MinecraftPacket packet = packetRegistry.decodePacket(packetId, buf);

                    if (packet != null) {
                        handlePacket(packet);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

            while (copiedBuf.refCnt() > 0) copiedBuf.release();

            MinecraftProxy.getInstance().getSchedulerExecutorService().addTask(() -> {
                while (copiedBuf2.refCnt() > 0) copiedBuf2.release();
                while (buf.refCnt() > 0) buf.release();
            }, 500, TimeUnit.MILLISECONDS);

            super.channelRead(ctx, buf);
        } else super.channelRead(ctx, msg);
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}