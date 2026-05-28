package com.mikedattolo.worldgen.generator;

import com.mikedattolo.worldgen.cache.ChunkSpatialIndex;
import com.mikedattolo.worldgen.io.PreparedWorldData;

public class DEMWorldGenerator {
    private final TerrainGenerator terrainGenerator = new TerrainGenerator();
    private final RoadGenerator roadGenerator = new RoadGenerator();

    public String generateChunk(PreparedWorldData data, ChunkSpatialIndex index, int chunkX, int chunkZ) {
        int[] samples = new int[]{0, 1, 2, 3, 4};
        int baseHeight = terrainGenerator.estimateBaseHeight(chunkX, chunkZ, samples, 1.0);
        int roads = index.getFeatureCount(chunkX, chunkZ);
        return "DEM chunk " + chunkX + "," + chunkZ + " baseHeight=" + baseHeight + " roads=" + roadGenerator.generateRoadPrototype(roads);
    }
}
