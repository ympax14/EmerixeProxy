package com.emerixe.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
            ByteBuf byteBuf = (ByteBuf) msg;
    
            try {
                int packetId = MinecraftPacket.readVarInt(byteBuf); // Lire l'ID du paquet

                // Intercepter le paquet "LoginStart" envoyé par le client
                if (packetId == 0x00) {
                    // Lire les données de l'handshake
                    int protocolVersion = MinecraftPacket.readVarInt(byteBuf); // Version du protocole
                    String serverAddress = MinecraftPacket.readString(byteBuf); // Adresse du serveur
                    int serverPort = byteBuf.readUnsignedShort(); // Port du serveur
                    int nextState = MinecraftPacket.readVarInt(byteBuf); // État suivant (1 = status, 2 = login)

                    // Afficher les informations de l'handshake pour debug
                    System.out.println("Handshake received:");
                    System.out.println("Protocol Version: " + protocolVersion);
                    System.out.println("Server Address: " + serverAddress);
                    System.out.println("Server Port: " + serverPort);
                    System.out.println("Next State: " + nextState);

                    // Logique pour rediriger ou gérer la connexion ici
                    if (nextState == 1) {
                        // Le client veut obtenir des informations sur le serveur (status)
                        System.out.println("Switching to status state");
                    } else if (nextState == 2) {
                        // Le client veut se connecter (login)
                        System.out.println("Switching to login state");
                    }
                } else {
                    System.out.println("Received packet with ID: " + packetId);
                    byteBuf.resetReaderIndex();
                    ctx.fireChannelRead(byteBuf);
                }
            } catch (Exception e) {
                System.out.println("A packet have been received but an error occured while reading it.");
                byteBuf.release();
                // TODO: handle exception
            }
        } else {
            ctx.fireChannelRead(msg);
        }
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