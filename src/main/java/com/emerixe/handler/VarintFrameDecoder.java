package com.emerixe.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

public class VarintFrameDecoder extends ByteToMessageDecoder {

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

    System.out.println("decode-VarintFrameDecoder-inputHex:" + ByteBufUtil.hexDump(in));

    in.markReaderIndex();
    byte[] buf = new byte[3];
    for (int i = 0; i < buf.length; i++) {
      if (!in.isReadable()) {
        in.resetReaderIndex();
        return;
      }
      buf[i] = in.readByte();
      if (buf[i] >= 0) {
        int length = readVarInt(buf);
        if (in.readableBytes() < length) {
          in.resetReaderIndex();
          return;
        }

        // Extract the full packet
        ByteBuf packet = in.readBytes(length);

        // Optionally, add the remaining data (without the ID) to the output
        out.add(packet);
        return;
      }
    }
    throw new RuntimeException("VarInt trop grand");
  }

  private int readVarInt(byte[] buf) {
    int numRead = 0;
    int result = 0;
    byte read;
    do {
      read = buf[numRead];
      int value = (read & 0b01111111);
      result |= (value << (7 * numRead));

      numRead++;
      if (numRead > 5) {
        throw new RuntimeException("VarInt est trop grand");
      }
    } while ((read & 0b10000000) != 0);

    return result;
  }
}