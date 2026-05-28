package com.mikedattolo.worldgen.cache;

import java.util.HashMap;
import java.util.Map;

public class ChunkFeatureCache {
    private final Map<Long, String> cache = new HashMap<Long, String>();

    public void put(int chunkX, int chunkZ, String payload) {
        cache.put(key(chunkX, chunkZ), payload);
    }

    public String get(int chunkX, int chunkZ) {
        return cache.get(key(chunkX, chunkZ));
    }

    private long key(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) | (chunkZ & 0xffffffffL);
    }
}
