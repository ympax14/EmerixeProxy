package com.emerixe.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;

public abstract class MinecraftPacket {
    private final int packetId;

    public MinecraftPacket(int packetId) {
        this.packetId = packetId;
    }

    public int getPacketId() {
        return packetId;
    }

    // Méthodes utilitaires pour encoder et décoder des types de données simples

    public static void writeVarInt(ByteBuf buf, int value) {
        while ((value & 0xFFFFFF80) != 0L) {
            buf.writeByte(value & 0x7F | 0x80);
            value >>>= 7;
        }
        buf.writeByte(value & 0x7F);
    }

    public static void writeUUID(ByteBuf buf, UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    public static void writeString(ByteBuf buf, String str) {
        byte[] bytes = str.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }

    public static int readVarInt(ByteBuf buf) {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = buf.readByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) throw new RuntimeException("VarInt trop long");
        } while ((read & 0b10000000) != 0);

        return result;
    }

    public static UUID readUUID(ByteBuf buf) {
        long mostSigBits = buf.readLong();
        long leastSigBits = buf.readLong();
        return new UUID(mostSigBits, leastSigBits);
    }

    public static String readString(ByteBuf buf) {
        int length = readVarInt(buf);
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    // Méthode pour décoder un paquet à partir du buffer
    public void decode(ByteBuf buf) {

    }

    public abstract void encode(ByteBuf buf);
}