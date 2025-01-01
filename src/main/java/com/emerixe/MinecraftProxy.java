package com.emerixe;

import com.emerixe.handler.ProxyMessageHandler;
import com.emerixe.handler.VarintFrameDecoder;
import com.emerixe.handler.VarintFrameEncoder;
import com.emerixe.manager.PlayerConnectionManager;
import com.emerixe.manager.RedisManager;
import com.emerixe.router.ServerRouter;
import com.emerixe.scheduler.SchedulerExecutorService;

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
    private final SchedulerExecutorService schedulerExecutorService;

    public MinecraftProxy(int port, String hubHost, int hubPort, String gameHost, int gamePort) {
        instance = this;

        this.port = port;
        this.redisManager = new RedisManager("localhost", 6379);
        this.serverRouter = new ServerRouter();
        this.playerConnectionManager = new PlayerConnectionManager();
        this.schedulerExecutorService = new SchedulerExecutorService(2);
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

    public SchedulerExecutorService getSchedulerExecutorService() {
        return schedulerExecutorService;
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
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                .addLast("varintFrameDecoder", new VarintFrameDecoder())
                                .addLast("varintFrameEncoder", new VarintFrameEncoder())
                                .addLast("proxyHandler", new ProxyMessageHandler());
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
        new MinecraftProxy(45654, "127.0.0.1", 18546, "127.0.0.1", 48216).start();
    }
}