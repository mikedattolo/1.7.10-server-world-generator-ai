package com.mikedattolo.worldgen.generator;

public class RoadGenerator {
    public String generateRoadPrototype(int roadsInChunk) {
        if (roadsInChunk <= 0) {
            return "no roads";
        }
        return roadsInChunk > 2 ? "major intersection prototype" : "local road prototype";
    }
}
