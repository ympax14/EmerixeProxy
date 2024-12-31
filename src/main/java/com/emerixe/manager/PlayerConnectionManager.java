package com.emerixe.manager;

import com.emerixe.MinecraftProxy;
import com.emerixe.initializer.ServerChannelInitializer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.net.SocketAddress;

public class PlayerConnectionManager {

    private final EventLoopGroup group = new NioEventLoopGroup();

    public PlayerConnectionManager() {

    }

    /**
     * Connecte un joueur à un serveur Minecraft spécifique.
     *
     * @param playerChannel Canal du joueur (client).
     * @param serverName    Nom du serveur cible (exemple : "hub", "minigame").
     */
    public void connectToServer(Channel playerChannel, String serverName) {
        SocketAddress targetServer = MinecraftProxy.getInstance().getServerRouter().getServer(serverName);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ServerChannelInitializer(playerChannel));

        ChannelFuture future = bootstrap.connect(targetServer);
        future.addListener((ChannelFuture f) -> {
            if (f.isSuccess()) {
                System.out.println("Connexion réussie au serveur " + targetServer);
            } else {
                System.err.println("Échec de la connexion au serveur " + targetServer);
                f.cause().printStackTrace();
            }
        });
    }

    /**
     * Arrête le gestionnaire de connexions proprement.
     */
    public void shutdown() {
        group.shutdownGracefully();
    }
}