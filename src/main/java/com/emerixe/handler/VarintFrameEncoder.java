package com.emerixe.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class VarintFrameEncoder extends MessageToByteEncoder<ByteBuf> {

  @Override
  protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
    int readableBytes = msg.readableBytes();
    writeVarInt(out, readableBytes);
    out.writeBytes(msg, msg.readerIndex(), readableBytes);
  }

  private void writeVarInt(ByteBuf out, int value) {
    while ((value & 0xFFFFFF80) != 0) {
      out.writeByte((value & 0x7F) | 0x80);
      value >>>= 7;
    }
    out.writeByte(value & 0x7F);
  }
}
