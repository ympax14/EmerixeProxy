package com.emerixe.router;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.emerixe.handler.MessageHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ServerRouter {
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final Map<String, InetSocketAddress> serverMap = new HashMap<>();

    public ServerRouter() {
        // Ajouter des serveurs à la carte (adresse et port des serveurs)
        serverMap.put("hub", new InetSocketAddress("127.0.0.1", 18546));
        serverMap.put("minigame", new InetSocketAddress("127.0.0.1", 48216));
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

    public void connectToServer(InetSocketAddress targetServer, Channel playerChannel, Consumer<Channel> onSuccess, Consumer<Throwable> onError) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                            // .addLast("varintFrameDecoder", new VarintFrameDecoder())
                            // .addLast("varintFrameEncoder", new VarintFrameEncoder())
                            // .addLast("varintFrame", new VarintFrameChecker())
                            // .addLast("varintFrameDecoder", new VarintFrameDecoder())
                            .addLast("messageHandler", new MessageHandler(playerChannel));
                    }
                });

        ChannelFuture future = bootstrap.connect(targetServer);

        future.addListener((ChannelFuture f) -> {
            if (f.isSuccess()) {
                MessageHandler messageHandler = new MessageHandler(playerChannel);
                messageHandler.setServerChannel(f.channel());
                messageHandler.linkChannels();

                f.channel().pipeline().replace(MessageHandler.class, "clientMessageHandler", messageHandler);

                System.out.println("Connexion réussie au serveur " + targetServer);
                onSuccess.accept(f.channel()); // Appel du callback de succès
            } else {
                System.err.println("Échec de la connexion au serveur " + targetServer);
                if (onError != null) onError.accept(f.cause()); // Appel du callback d'erreur
                f.cause().printStackTrace();
            }
        });
    }
}