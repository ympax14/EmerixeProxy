package com.emerixe.packet.packets;

import com.emerixe.packet.MinecraftPacket;

import io.netty.buffer.ByteBuf;
import java.util.UUID;

public class LoginStartPacket extends MinecraftPacket {
    private UUID playerUUID;
    private String playerName;
    private String originServer;

    public LoginStartPacket() {
        super(0); // ID du paquet Login Start
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getOriginServer() {
        return originServer;
    }

    @Override
    public void encode(ByteBuf buf) {
        writeVarInt(buf, this.getPacketId()); // ID du paquet
        
        // Encode l'UUID du joueur
        writeUUID(buf, playerUUID);

        // Encode le nom du joueur
        writeString(buf, playerName);

        // Encode le nom du serveur
        writeString(buf, originServer);
    }

    @Override
    public void decode(ByteBuf buf) {
        // Décodage de l'UUID du joueur
        this.playerUUID = readUUID(buf);

        // Décodage du nom du joueur
        this.playerName = readString(buf);

        // Décodage du nom du serveur
        this.originServer = readString(buf);
    }
    
}