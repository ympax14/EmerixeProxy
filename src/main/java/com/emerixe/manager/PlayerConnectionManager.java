package com.emerixe.manager;

import com.emerixe.MinecraftProxy;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PlayerConnectionManager {
    private final Map<Channel, Channel> serverChannelMap = new HashMap<>();

    public PlayerConnectionManager() {

    }

    /**
     * Connecte un joueur à un serveur Minecraft spécifique.
     *
     * @param targetServerName Nom du serveur cible (exemple : "hub", "minigame"). 
     * @param playerChannel    Canal du joueur (client).
     * @param onSuccess        Callback lors de la connexion réussite
     * @param onError          Callback lors de la connexion ratée
     */
    public void connectToServer(String targetServerName, Channel playerChannel, Consumer<Channel> onSuccess, Consumer<Throwable> onError) {
        InetSocketAddress targetServer = MinecraftProxy.getInstance().getServerRouter().getServer(targetServerName);
        MinecraftProxy.getInstance().getServerRouter().connectToServer(targetServer, playerChannel, channel -> {

            if (serverChannelMap.containsKey(playerChannel)) {
                Channel chnl = serverChannelMap.get(playerChannel);
                if (chnl.pipeline().last() == null) {
                    chnl.close();
                    System.out.println("Canaux de " + playerChannel.remoteAddress() + " vers " + chnl.remoteAddress() + " fermé.");
                }
            }

            serverChannelMap.put(playerChannel, channel);
            if (onSuccess != null) onSuccess.accept(channel);
        }, onError);
    }

    public void clearRemoteAddress(SocketAddress address) {
        Channel channel = this.getPlayerChannelFromRemoteAddress(address);
        if (this.getServerChannelMap().containsKey(channel)) {
            this.getServerChannelMap().remove(channel);
            this.getServerConnectedToPlayer(channel).close();
            channel.close();
        }
    }

    public List<Channel> getPlayersConnectedToServer(SocketAddress address) {
        List<Channel> keys = new ArrayList<>();
        for (Map.Entry<Channel, Channel> entry : this.getServerChannelMap().entrySet()) {
            if (entry.getValue().remoteAddress().toString().split(":")[0].equals(address.toString().split(":")[0])) {
                keys.add(entry.getKey());
            }
        }

        return keys;
    }

    public Channel getPlayerConnectedToServer(SocketAddress address) {
        for (Map.Entry<Channel, Channel> entry : this.getServerChannelMap().entrySet()) {
            if (entry.getValue().remoteAddress().equals(address)) {
                return entry.getKey();
            }
        }

        return null;
    }

    public Channel getPlayerChannelFromRemoteAddress(SocketAddress address) {
        for (Channel channel : this.getServerChannelMap().keySet()) {
            if (channel.remoteAddress().toString().split(":")[0] == address.toString().split(":")[0]) return channel;
        }

        return null;
    }

    public Channel getServerConnectedToPlayer(Channel channel) {
        return serverChannelMap.getOrDefault(channel, null);
    }


    public Channel getServerConnectedToPlayer(SocketAddress address) {
        return serverChannelMap.getOrDefault(this.getPlayerChannelFromRemoteAddress(address), null);
    }

    public Boolean playerIsAlreadyConnected(SocketAddress address) {
        return getPlayerChannelFromRemoteAddress(address) != null;
    }

    public Map<Channel, Channel> getServerChannelMap() {
        return this.serverChannelMap;
    }

    /**
     * Arrête le gestionnaire de connexions proprement.
     */
    public void shutdown() {
        serverChannelMap.forEach((player, server) -> {
            server.close();
            player.close();
        });
        serverChannelMap.clear();
    }
}