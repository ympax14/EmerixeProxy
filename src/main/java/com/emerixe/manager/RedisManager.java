package com.emerixe.manager;

import redis.clients.jedis.Jedis;

public class RedisManager {
    private final Jedis jedis;

    public RedisManager(String host, int port) {
        this.jedis = new Jedis(host, port);
    }

    public void set(String path, String data) {
        jedis.set(path, data);
    }

    public String get(String path) {
        return jedis.get(path);
    }

    public void close() {
        jedis.close();
    }
}