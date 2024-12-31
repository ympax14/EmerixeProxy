package com.emerixe.packet.packets;

import com.emerixe.packet.MinecraftPacket;

import io.netty.buffer.ByteBuf;
import java.util.UUID;

public class ServerTransferPacket extends MinecraftPacket {
    private UUID playerUUID;       // UUID du joueur qui sera transféré
    private String playerName;     // Nom du joueur qui sera transféré
    private String targetServer;    // Nom du serveur de destination

    public ServerTransferPacket() {
        super(1);  // Choisissez un ID de paquet unique pour ce paquet de transfert
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getTargetServer() {
        return targetServer;
    }

    public void setTargetServer(String targetServer) {
        this.targetServer = targetServer;
    }

    @Override
    public void encode(ByteBuf buf) {
        writeVarInt(buf, this.getPacketId());  // ID du paquet
        
        // Encode l'UUID du joueur
        writeUUID(buf, playerUUID);

        // Encode le nom du joueur
        writeString(buf, playerName);

        // Encode le nom du serveur de destination
        writeString(buf, targetServer);
    }

    @Override
    public void decode(ByteBuf buf) {
        // Décodage de l'UUID du joueur
        this.playerUUID = readUUID(buf);

        // Décodage du nom du joueur
        this.playerName = readString(buf);

        // Décodage du nom du serveur de destination
        this.targetServer = readString(buf);
    }
}