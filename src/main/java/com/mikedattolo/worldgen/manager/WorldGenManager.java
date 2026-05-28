package com.mikedattolo.worldgen.manager;

import com.mikedattolo.worldgen.cache.ChunkFeatureCache;
import com.mikedattolo.worldgen.cache.ChunkSpatialIndex;
import com.mikedattolo.worldgen.generator.DEMWorldGenerator;
import com.mikedattolo.worldgen.generator.PromptWorldGenerator;
import com.mikedattolo.worldgen.io.PreparedWorldData;
import com.mikedattolo.worldgen.io.PreparedWorldDataLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;

public class WorldGenManager {
    private static final Logger LOGGER = Logger.getLogger(WorldGenManager.class.getName());

    private final PreparedWorldDataLoader loader = new PreparedWorldDataLoader();
    private final ChunkFeatureCache cache = new ChunkFeatureCache();
    private final ChunkSpatialIndex spatialIndex = new ChunkSpatialIndex();
    private final DEMWorldGenerator demGenerator = new DEMWorldGenerator();
    private final PromptWorldGenerator promptGenerator = new PromptWorldGenerator();

    private PreparedWorldData data;

    /**
     * Loads externally prepared map artifacts. Runtime intentionally avoids networking and heavy
     * preprocessing, and only consumes pre-built files for chunk-time generation.
     */
    public void loadPreparedData(Path directory) throws IOException {
        data = loader.load(directory);
        for (Map.Entry<String, Integer> entry : data.featureCounts.entrySet()) {
            LOGGER.info("Loaded " + entry.getKey() + " features=" + entry.getValue());
        }
        int totalRoadFeatures = data.featureCounts.getOrDefault("roads.geojson", 0);
        int chunksPerAxis = Math.max(1, data.worldSize / 16);
        for (int i = 0; i < totalRoadFeatures; i++) {
            int chunkX = i % chunksPerAxis;
            int chunkZ = (i / chunksPerAxis) % chunksPerAxis;
            spatialIndex.indexChunk(chunkX, chunkZ, spatialIndex.getFeatureCount(chunkX, chunkZ) + 1);
        }
    }

    public String generateChunk(int chunkX, int chunkZ) {
        if (data == null) {
            return "No prepared data loaded";
        }
        String cached = cache.get(chunkX, chunkZ);
        if (cached != null) {
            return cached;
        }
        String result = "DEM".equalsIgnoreCase(data.mode)
                ? demGenerator.generateChunk(data, spatialIndex, chunkX, chunkZ)
                : promptGenerator.generateChunk(data, chunkX, chunkZ);
        cache.put(chunkX, chunkZ, result);
        return result;
    }
}
