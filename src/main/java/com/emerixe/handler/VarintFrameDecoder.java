package com.emerixe.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class VarintFrameDecoder extends ByteToMessageDecoder {
  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf input, List<Object> out) {
    input.markReaderIndex();

    int length = readVarInt(input);
    if (length == -1) {
      input.resetReaderIndex();
      return;
    }

    if (input.readableBytes() < length) {
      input.resetReaderIndex();
      return;
    }

    ByteBuf frame = input.readBytes(length);
    out.add(frame);
  }

  /**
   * Lit un VarInt depuis le ByteBuf.
   * Retourne -1 si on ne peut pas encore le lire entièrement.
   */
  private int readVarInt(ByteBuf buf) {
    int value = 0;
    int position = 0;
    while (true) {
      if (!buf.isReadable()) {
        return -1;
      }

      byte b = buf.readByte();
      value |= (b & 0x7F) << (position++ * 7);

      if (position > 5) {
        throw new RuntimeException("VarInt trop grand (corrompu).");
      }
      if ((b & 0x80) == 0) {
        break;
      }
    }
    return value;
  }
}
