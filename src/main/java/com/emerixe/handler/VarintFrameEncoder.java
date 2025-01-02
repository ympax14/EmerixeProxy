
package com.emerixe.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class VarintFrameEncoder extends MessageToByteEncoder<ByteBuf> {

  @Override
  protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
    int bodyLength = msg.readableBytes();
    int headerLength = getVarIntSize(bodyLength);
    out.ensureWritable(headerLength + bodyLength);
    writeVarInt(out, bodyLength);
    out.writeBytes(msg);
  }

  private void writeVarInt(ByteBuf out, int value) {
    while ((value & 0xFFFFFF80) != 0L) {
      out.writeByte((value & 0x7F) | 0x80);
      value >>>= 7;
    }
    out.writeByte(value & 0x7F);
  }

  private int getVarIntSize(int value) {
    int size = 0;
    do {
      size++;
      value >>>= 7;
    } while (value != 0);
    return size;
  }
}