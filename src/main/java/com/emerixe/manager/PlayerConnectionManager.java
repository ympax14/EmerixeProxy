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

        if (serverChannelMap.containsKey(playerChannel)) {
            Channel chnl = serverChannelMap.get(playerChannel);
            if (chnl.pipeline().last() == null) {
                chnl.close();
                if (chnl.remoteAddress() != null) {
                    SocketAddress remoteAddress = chnl.remoteAddress();
                    System.out.println("Canaux de " + playerChannel.remoteAddress() + " vers " + remoteAddress.toString() + " fermé.");
                }
            }
        }

        MinecraftProxy.getInstance().getServerRouter().connectToServer(targetServer, playerChannel, channel -> {
            serverChannelMap.put(playerChannel, channel);
            if (onSuccess != null) onSuccess.accept(channel);
        }, onError);
    }

    public void clearRemoteAddress(SocketAddress address) {
        Channel channel = this.getPlayerChannelFromRemoteAddress(address);
        if (this.getServerChannelMap().containsKey(channel)) {
            this.getServerChannelMap().remove(channel);

            Channel chnl = this.getServerConnectedToPlayer(channel);

            if (chnl != null) chnl.close(); // On vérifie si le channel correspond à celui d'un Serveur, si oui on le ferme.
            channel.close();
        }
    }

    public List<Channel> getPlayersConnectedToServer(SocketAddress address) {
        List<Channel> keys = new ArrayList<>();
        for (Map.Entry<Channel, Channel> entry : this.getServerChannelMap().entrySet()) {
            if (entry.getValue().remoteAddress().toString().split(":")[0].replace("/", "").equals(address.toString().split(":")[0].replace("/", ""))) {
                keys.add(entry.getKey());
            }
        }

        return keys;
    }

    public Channel getPlayerConnectedToServer(SocketAddress address) {
        Channel playerChannel = null;

        for (Map.Entry<Channel, Channel> entry : this.getServerChannelMap().entrySet()) {
            if (entry.getValue().remoteAddress().equals(address)) {
                playerChannel = entry.getKey();
                break;
            }
        }

        return playerChannel;
    }

    public Channel getPlayerChannelFromRemoteAddress(SocketAddress address) {
        Channel playerChannel = null;

        for (Channel channel : this.getServerChannelMap().keySet()) {
            if (channel.remoteAddress().toString().split(":")[0].replace("/", "").equals(address.toString().split(":")[0].replace("/", ""))) {
                playerChannel = channel;
                break;
            }
        }

        return playerChannel;
    }

    public Channel getServerConnectedToPlayer(Channel channel) {
        return serverChannelMap.getOrDefault(channel, null);
    }


    public Channel getServerConnectedToPlayer(SocketAddress address) {
        return serverChannelMap.getOrDefault(this.getPlayerChannelFromRemoteAddress(address), null);
    }

    /**
     * Vérifie si un joueur est déjà connecté.
     *
     * @param address Adresse du joueur
     * @return Vrai si le joueur est déjà connecté, faux sinon
     */
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