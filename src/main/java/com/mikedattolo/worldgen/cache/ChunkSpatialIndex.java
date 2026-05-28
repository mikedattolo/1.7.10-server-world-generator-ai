package com.mikedattolo.worldgen.cache;

import java.util.HashMap;
import java.util.Map;

public class ChunkSpatialIndex {
    private final Map<Long, Integer> featureCountByChunk = new HashMap<Long, Integer>();

    public void indexChunk(int chunkX, int chunkZ, int featureCount) {
        featureCountByChunk.put(key(chunkX, chunkZ), featureCount);
    }

    public int getFeatureCount(int chunkX, int chunkZ) {
        Integer v = featureCountByChunk.get(key(chunkX, chunkZ));
        return v == null ? 0 : v;
    }

    private long key(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) | (chunkZ & 0xffffffffL);
    }
}
