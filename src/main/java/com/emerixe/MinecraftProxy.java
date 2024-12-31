package com.emerixe;

import com.emerixe.handler.ProxyMessageHandler;
import com.emerixe.manager.PlayerConnectionManager;
import com.emerixe.manager.RedisManager;
import com.emerixe.router.ServerRouter;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class MinecraftProxy {
    private static MinecraftProxy instance;

    private final int port;
    private final RedisManager redisManager;
    private final ServerRouter serverRouter;
    private final PlayerConnectionManager playerConnectionManager;

    public MinecraftProxy(int port, String hubHost, int hubPort, String gameHost, int gamePort) {
        instance = this;

        this.port = port;
        this.redisManager = new RedisManager("localhost", 6379);
        this.serverRouter = new ServerRouter();
        this.playerConnectionManager = new PlayerConnectionManager();
    }

    public static MinecraftProxy getInstance() {
        return instance;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public ServerRouter getServerRouter() {
        return serverRouter;
    }

    public PlayerConnectionManager getPlayerConnectionManager() {
        return playerConnectionManager;
    }

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProxyMessageHandler());
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("Proxy Minecraft en écoute sur le port " + port);
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new MinecraftProxy(25565, "127.0.0.1", 25566, "127.0.0.1", 25567).start();
    }
}