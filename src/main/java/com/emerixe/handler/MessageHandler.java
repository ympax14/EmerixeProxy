package com.emerixe.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MessageHandler extends ChannelInboundHandlerAdapter {
    private final Channel clientChannel;
    private Channel serverChannel;

    public MessageHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            buf.retain();
            super.channelRead(ctx, buf);
        } else super.channelRead(ctx, msg);
    }

    public void setServerChannel(Channel channel) {
        this.serverChannel = channel;
    }

    public void linkChannels() {
         // Relier les canaux client et serveur
         serverChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext c, Object msg) throws Exception {
                // Relayer les paquets du serveur au client
                if (clientChannel.isActive()) {
                    clientChannel.writeAndFlush(msg);  // Relayer le paquet reçu du serveur vers le client
                }
                super.channelRead(c, msg);
            }
        });

        clientChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext c, Object msg) throws Exception {
                // Relayer les paquets du client au serveur
                if (serverChannel.isActive()) {
                    serverChannel.writeAndFlush(msg);  // Relayer le paquet reçu du client vers le serveur
                }
                super.channelRead(c, msg);
            }
        });
    }
}