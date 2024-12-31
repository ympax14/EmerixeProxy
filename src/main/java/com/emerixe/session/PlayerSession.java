package com.emerixe.session;

import java.util.UUID;

public class PlayerSession {
    private final UUID playerUUID;
    private String origin;  // "proxy", "hub" etc...
    private final long sessionId; // Identifiant unique de la session

    public PlayerSession(UUID playerUUID, String origin) {
        this.playerUUID = playerUUID;
        this.origin = origin;
        this.sessionId = System.currentTimeMillis();  // Exemple de session ID basé sur l'heure actuelle
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public long getSessionId() {
        return sessionId;
    }
}
