package com.mikedattolo.worldgen.manager;

public class StructureManager {
    public boolean shouldPlaceStructure(int chunkX, int chunkZ, int rarity) {
        return Math.abs(chunkX * 13 + chunkZ * 7) % Math.max(1, rarity) == 0;
    }
}
