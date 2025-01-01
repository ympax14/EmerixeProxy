package com.emerixe.handler;

import java.util.concurrent.TimeUnit;

import com.emerixe.MinecraftProxy;

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
                if (msg instanceof ByteBuf) {
                    ByteBuf buf = (ByteBuf) msg;

                    buf.retain();

                    if (clientChannel.isActive()) {
                        clientChannel.writeAndFlush(buf);  // Relayer le paquet reçu du serveur vers le client
                    }

                    MinecraftProxy.getInstance().getSchedulerExecutorService().addTask(() -> {
                        while (buf.refCnt() > 0) buf.release();
                    }, 500, TimeUnit.MILLISECONDS);
                } else super.channelRead(c, msg);
            }
        })
        .addLast(new IdleHandler(0, 60, 0, false)); // 0 secondes pour READER_IDLE, 60 secondes pour WRITER_IDLE, 0 secondes pour ALL_IDLE

        clientChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext c, Object msg) throws Exception {
                // Relayer les paquets du client au serveur
                if (msg instanceof ByteBuf) {
                    ByteBuf buf = (ByteBuf) msg;

                    buf.retain();

                    if (serverChannel.isActive()) {
                        serverChannel.writeAndFlush(buf);  // Relayer le paquet reçu du serveur vers le client
                    }

                    MinecraftProxy.getInstance().getSchedulerExecutorService().addTask(() -> {
                        while (buf.refCnt() > 0) buf.release();
                    }, 500, TimeUnit.MILLISECONDS);
                } else super.channelRead(c, msg);
            }
        })
        .addLast(new IdleHandler(0, 60, 0, true)); // 0 secondes pour READER_IDLE, 60 secondes pour WRITER_IDLE, 0 secondes pour ALL_IDLE
    }
}