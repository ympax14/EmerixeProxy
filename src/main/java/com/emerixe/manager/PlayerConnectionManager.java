package com.emerixe.manager;

import com.emerixe.MinecraftProxy;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

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
    public void connectToServer(String targetServerName, Channel playerChannel, Consumer<Channel> onSuccess, Consumer<Throwable> onError) {
        InetSocketAddress targetServer = MinecraftProxy.getInstance().getServerRouter().getServer(targetServerName);
        MinecraftProxy.getInstance().getServerRouter().connectToServer(targetServer, playerChannel, onSuccess, onError);
    }

    /**
     * Arrête le gestionnaire de connexions proprement.
     */
    public void shutdown() {
        group.shutdownGracefully();
    }
}