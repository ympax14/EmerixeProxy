package com.emerixe.initializer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;

public class ServerChannelInitializer extends ChannelInitializer<Channel> {
    private final Channel clientChannel;

    public ServerChannelInitializer(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    protected void initChannel(Channel serverChannel) throws Exception {
        // Relier les canaux client et serveur
        serverChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                // Relayer les paquets du serveur au client
                if (clientChannel.isActive()) {
                    clientChannel.writeAndFlush(msg);  // Relayer le paquet reçu du serveur vers le client
                }
                super.channelRead(ctx, msg);
            }
        });

        clientChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                // Relayer les paquets du client au serveur
                if (serverChannel.isActive()) {
                    serverChannel.writeAndFlush(msg);  // Relayer le paquet reçu du client vers le serveur
                }
                super.channelRead(ctx, msg);
            }
        });
    }
}