package com.mikedattolo.worldgen.generator;

import com.mikedattolo.worldgen.io.PreparedWorldData;

public class PromptWorldGenerator {
    public String generateChunk(PreparedWorldData data, int chunkX, int chunkZ) {
        return "Prompt chunk " + chunkX + "," + chunkZ + " style=" + data.style;
    }
}
