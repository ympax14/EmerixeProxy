package com.emerixe.router;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.emerixe.handler.ProxyMessageHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ServerRouter {
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final Map<String, InetSocketAddress> serverMap = new HashMap<>();

    public ServerRouter() {
        // Ajouter des serveurs à la carte (adresse et port des serveurs)
        serverMap.put("hub", new InetSocketAddress("127.0.0.1", 25566));
        serverMap.put("minigame", new InetSocketAddress("127.0.0.1", 25567));
    }

    /**
     * Obtenir l'adresse du serveur cible basé sur le nom.
     *
     * @param serverName Nom du serveur (exemple : "hub", "minigame").
     * @return Adresse et port du serveur cible.
     */
    public InetSocketAddress getServer(String serverName) {
        return serverMap.getOrDefault(serverName, serverMap.get("hub")); // Serveur par défaut : hub
    }

    public Channel openServerChannel(InetSocketAddress targetServer) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        // Ajouter les handlers nécessaires pour gérer les paquets
                        pipeline.addLast(new ProxyMessageHandler());
                    }
                });

        ChannelFuture future = bootstrap.connect(targetServer);
        
        future.addListener((ChannelFuture f) -> {
            if (f.isSuccess()) {
                System.out.println("Connexion réussie au serveur " + targetServer);                
            } else {
                System.err.println("Échec de la connexion au serveur " + targetServer);
                f.cause().printStackTrace();
            }
        });

        return future.channel();
    }
}