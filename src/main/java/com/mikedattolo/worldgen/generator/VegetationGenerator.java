package com.mikedattolo.worldgen.generator;

public class VegetationGenerator {
    public int estimateTreesForChunk(int vegetationFeatures, String style) {
        int base = vegetationFeatures * 8;
        if ("apocalypse".equalsIgnoreCase(style)) {
            return base + 12;
        }
        return base;
    }
}
