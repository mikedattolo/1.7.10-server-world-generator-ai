package com.mikedattolo.worldgen.generator;

public class TerrainGenerator {
    public int estimateBaseHeight(int chunkX, int chunkZ, int[] elevationSamples, double verticalScale) {
        if (elevationSamples.length == 0) {
            return 64;
        }
        int sample = elevationSamples[Math.abs((chunkX * 31 + chunkZ * 17) % elevationSamples.length)];
        return 64 + (int) (sample * verticalScale);
    }
}
