package com.emerixe.handler;

import java.net.SocketAddress;

import com.emerixe.MinecraftProxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

public class IdleHandler extends IdleStateHandler {
    private boolean isClient;

    public IdleHandler(Integer readerIdleTime, Integer writerIdleTime, Integer allIdleTime, boolean isClient) {
        super(readerIdleTime, writerIdleTime, allIdleTime);
        this.isClient = isClient;
    }

    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        if (evt.state().equals(IdleState.WRITER_IDLE)) {
            if (isClient) {
                System.out.println("Le canal " + ctx.channel().remoteAddress() + " a été fermé car le Client ne répond pas. (écriture)");
                MinecraftProxy.getInstance().getPlayerConnectionManager().clearRemoteAddress(ctx.channel().remoteAddress());
            } else {
                SocketAddress remoteAddress = ctx.channel().remoteAddress();
                String remoteAddreString = remoteAddress.toString();
                Channel channel = MinecraftProxy.getInstance().getPlayerConnectionManager().getPlayerConnectedToServer(remoteAddress);
                ctx.close();
                MinecraftProxy.getInstance().getPlayerConnectionManager().connectToServer("hub", channel, null, null);
                System.out.println("Le canal " + remoteAddreString + " a été fermé car le Client ne répond pas. (écriture)");
            }
        }
        
        super.channelIdle(ctx, evt);
    }
}
