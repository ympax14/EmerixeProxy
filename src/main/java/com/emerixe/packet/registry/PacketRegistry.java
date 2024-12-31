package com.emerixe.packet.registry;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;

import com.emerixe.packet.MinecraftPacket;
import com.emerixe.packet.packets.LoginStartPacket;
import com.emerixe.packet.packets.ServerTransferPacket;

public class PacketRegistry {
    private final Map<Integer, Class<? extends MinecraftPacket>> packetMap = new HashMap<>();

    public PacketRegistry() {
        // Enregistrement des paquets
        registerPacket(0, LoginStartPacket.class);
        registerPacket(1, ServerTransferPacket.class);
        // Ajouter d'autres paquets ici
    }

    private void registerPacket(int packetId, Class<? extends MinecraftPacket> packetClass) {
        packetMap.put(packetId, packetClass);
    }

    public MinecraftPacket decodePacket(int packetId, ByteBuf buf) throws Exception {
        Class<? extends MinecraftPacket> packetClass = packetMap.get(packetId);
        if (packetClass == null) {
            System.out.println("Paquet inconnu : " + packetId);
            return null;
        }

        MinecraftPacket packet = packetClass.getDeclaredConstructor().newInstance();
        packet.decode(buf);
        return packet;
    }
}