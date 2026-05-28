package com.mikedattolo.worldbuilder.minecraft;

import com.mikedattolo.worldbuilder.dem.TerrariumElevationProvider;
import com.mikedattolo.worldbuilder.model.ProjectMetadata;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

public class MinecraftWorldExporter {
    private static final int TAG_END = 0;
    private static final int TAG_BYTE = 1;
    private static final int TAG_SHORT = 2;
    private static final int TAG_INT = 3;
    private static final int TAG_LONG = 4;
    private static final int TAG_BYTE_ARRAY = 7;
    private static final int TAG_STRING = 8;
    private static final int TAG_LIST = 9;
    private static final int TAG_COMPOUND = 10;
    private static final int TAG_INT_ARRAY = 11;
    private final TerrariumElevationProvider elevationProvider = new TerrariumElevationProvider();

    public void export(Path worldDir, ProjectMetadata metadata) throws IOException {
        Files.createDirectories(worldDir.resolve("region"));
        writeSessionLock(worldDir.resolve("session.lock"));
        writeLevelDat(worldDir.resolve("level.dat"), metadata);
        writeRegion(worldDir.resolve("region").resolve("r.0.0.mca"), metadata);
    }

    private void writeSessionLock(Path path) throws IOException {
        try (OutputStream stream = Files.newOutputStream(path); DataOutputStream out = new DataOutputStream(stream)) {
            out.writeLong(System.currentTimeMillis());
        }
    }

    private void writeLevelDat(Path path, ProjectMetadata metadata) throws IOException {
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(raw))) {
            namedCompound(out, "");
            namedCompound(out, "Data");
            namedLong(out, "RandomSeed", 123456789L);
            namedInt(out, "SpawnX", 8);
            namedInt(out, "SpawnY", 80);
            namedInt(out, "SpawnZ", 8);
            namedLong(out, "Time", 0L);
            namedLong(out, "LastPlayed", System.currentTimeMillis());
            namedString(out, "LevelName", metadata.projectName == null ? "WorldBuilder" : metadata.projectName);
            namedInt(out, "version", 19133);
            namedInt(out, "GameType", 0);
            namedByte(out, "MapFeatures", 1);
            namedByte(out, "hardcore", 0);
            namedByte(out, "allowCommands", 1);
            namedByte(out, "initialized", 1);
            end(out);
            end(out);
        }
        Files.write(path, raw.toByteArray());
    }

    private void writeRegion(Path path, ProjectMetadata metadata) throws IOException {
        byte[] header = new byte[8192];
        ByteArrayOutputStream sectors = new ByteArrayOutputStream();
        int sectorOffset = 2;
        int timestamp = (int) (System.currentTimeMillis() / 1000L);

        for (int chunkZ = 0; chunkZ < 32; chunkZ++) {
            for (int chunkX = 0; chunkX < 32; chunkX++) {
                byte[] chunk = compressedChunk(chunkX, chunkZ, metadata);
                int sectorCount = (chunk.length + 4095) / 4096;
                int index = chunkX + chunkZ * 32;
                header[index * 4] = (byte) ((sectorOffset >> 16) & 0xFF);
                header[index * 4 + 1] = (byte) ((sectorOffset >> 8) & 0xFF);
                header[index * 4 + 2] = (byte) (sectorOffset & 0xFF);
                header[index * 4 + 3] = (byte) (sectorCount & 0xFF);

                int timestampIndex = 4096 + index * 4;
                header[timestampIndex] = (byte) ((timestamp >> 24) & 0xFF);
                header[timestampIndex + 1] = (byte) ((timestamp >> 16) & 0xFF);
                header[timestampIndex + 2] = (byte) ((timestamp >> 8) & 0xFF);
                header[timestampIndex + 3] = (byte) (timestamp & 0xFF);

                sectors.write(chunk);
                int padding = sectorCount * 4096 - chunk.length;
                if (padding > 0) {
                    sectors.write(new byte[padding]);
                }
                sectorOffset += sectorCount;
            }
        }

        ByteArrayOutputStream region = new ByteArrayOutputStream();
        region.write(header);
        region.write(sectors.toByteArray());
        Files.write(path, region.toByteArray());
    }

    private byte[] compressedChunk(int chunkX, int chunkZ, ProjectMetadata metadata) throws IOException {
        ByteArrayOutputStream nbt = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(nbt)) {
            writeChunkNbt(out, chunkX, chunkZ, metadata);
        }

        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (DeflaterOutputStream deflater = new DeflaterOutputStream(compressed)) {
            deflater.write(nbt.toByteArray());
        }
        byte[] payload = compressed.toByteArray();

        ByteArrayOutputStream chunk = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(chunk)) {
            out.writeInt(payload.length + 1);
            out.writeByte(2);
            out.write(payload);
        }
        return chunk.toByteArray();
    }

    private void writeChunkNbt(DataOutputStream out, int chunkX, int chunkZ, ProjectMetadata metadata) throws IOException {
        namedCompound(out, "");
        namedCompound(out, "Level");
        namedInt(out, "xPos", chunkX);
        namedInt(out, "zPos", chunkZ);
        namedLong(out, "LastUpdate", 0L);
        namedByte(out, "TerrainPopulated", 1);
        namedByte(out, "LightPopulated", 1);
        namedLong(out, "InhabitedTime", 0L);
        namedIntArray(out, "HeightMap", heightMap(chunkX, chunkZ, metadata));
        namedByteArray(out, "Biomes", biomes(chunkX, chunkZ, metadata));

        namedListHeader(out, "Sections", TAG_COMPOUND, 8);
        for (int sectionY = 0; sectionY < 8; sectionY++) {
            writeSection(out, sectionY, chunkX, chunkZ, metadata);
        }
        namedListHeader(out, "Entities", TAG_COMPOUND, 0);
        namedListHeader(out, "TileEntities", TAG_COMPOUND, 0);
        namedListHeader(out, "TileTicks", TAG_COMPOUND, 0);
        end(out);
        end(out);
    }

    private void writeSection(DataOutputStream out, int sectionY, int chunkX, int chunkZ, ProjectMetadata metadata) throws IOException {
        namedByte(out, "Y", sectionY);
        byte[] blocks = new byte[4096];
        byte[] data = new byte[2048];
        byte[] blockLight = new byte[2048];
        byte[] skyLight = new byte[2048];
        Arrays.fill(skyLight, (byte) 0xFF);

        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                int height = terrainHeight(chunkX * 16 + x, chunkZ * 16 + z, metadata);
                for (int y = sectionY * 16; y < sectionY * 16 + 16; y++) {
                    int index = ((y & 15) << 8) | (z << 4) | x;
                    blocks[index] = blockFor(y, height);
                }
            }
        }
        namedByteArray(out, "Blocks", blocks);
        namedByteArray(out, "Data", data);
        namedByteArray(out, "BlockLight", blockLight);
        namedByteArray(out, "SkyLight", skyLight);
        end(out);
    }

    private int[] heightMap(int chunkX, int chunkZ, ProjectMetadata metadata) {
        int[] heights = new int[256];
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                heights[z * 16 + x] = terrainHeight(chunkX * 16 + x, chunkZ * 16 + z, metadata) + 1;
            }
        }
        return heights;
    }

    private byte[] biomes(int chunkX, int chunkZ, ProjectMetadata metadata) {
        byte[] biomes = new byte[256];
        byte biome = (byte) ("apocalypse".equalsIgnoreCase(metadata.style) ? 6 : 4);
        Arrays.fill(biomes, biome);
        return biomes;
    }

    private int terrainHeight(int worldX, int worldZ, ProjectMetadata metadata) {
        Double demElevation = demElevation(worldX, worldZ, metadata);
        if (demElevation != null) {
            double normalized = Math.max(-100.0, Math.min(2400.0, demElevation));
            return Math.max(45, Math.min(125, 58 + (int) Math.round(normalized / 32.0 * Math.max(0.5, metadata.verticalScale))));
        }
        double scale = Math.max(0.5, metadata.verticalScale);
        double ridges = Math.sin(worldX * 0.045) * 14.0 * scale;
        double valleys = Math.cos(worldZ * 0.038) * 10.0 * scale;
        double diagonal = Math.sin((worldX + worldZ) * 0.018) * 7.0 * scale;
        int height = (int) Math.round(68 + ridges + valleys + diagonal);
        return Math.max(48, Math.min(112, height));
    }

    private Double demElevation(int worldX, int worldZ, ProjectMetadata metadata) {
        if (metadata.bbox == null || metadata.bbox.isEmpty()) {
            return null;
        }
        Double minLat = metadata.bbox.get("minLat");
        Double minLon = metadata.bbox.get("minLon");
        Double maxLat = metadata.bbox.get("maxLat");
        Double maxLon = metadata.bbox.get("maxLon");
        if (minLat == null || minLon == null || maxLat == null || maxLon == null) {
            return null;
        }
        double sampleX = Math.max(0.0, Math.min(1.0, worldX / 511.0));
        double sampleZ = Math.max(0.0, Math.min(1.0, worldZ / 511.0));
        double lat = maxLat - (maxLat - minLat) * sampleZ;
        double lon = minLon + (maxLon - minLon) * sampleX;
        return elevationProvider.elevationMeters(lat, lon);
    }

    private byte blockFor(int y, int height) {
        if (y > height) {
            return 0;
        }
        if (y == height) {
            return 2;
        }
        if (y >= height - 3) {
            return 3;
        }
        if (y == 0) {
            return 7;
        }
        return 1;
    }

    private static void namedCompound(DataOutputStream out, String name) throws IOException {
        out.writeByte(TAG_COMPOUND);
        out.writeUTF(name);
    }

    private static void namedByte(DataOutputStream out, String name, int value) throws IOException {
        out.writeByte(TAG_BYTE);
        out.writeUTF(name);
        out.writeByte(value);
    }

    private static void namedInt(DataOutputStream out, String name, int value) throws IOException {
        out.writeByte(TAG_INT);
        out.writeUTF(name);
        out.writeInt(value);
    }

    private static void namedLong(DataOutputStream out, String name, long value) throws IOException {
        out.writeByte(TAG_LONG);
        out.writeUTF(name);
        out.writeLong(value);
    }

    private static void namedString(DataOutputStream out, String name, String value) throws IOException {
        out.writeByte(TAG_STRING);
        out.writeUTF(name);
        out.writeUTF(value);
    }

    private static void namedByteArray(DataOutputStream out, String name, byte[] value) throws IOException {
        out.writeByte(TAG_BYTE_ARRAY);
        out.writeUTF(name);
        out.writeInt(value.length);
        out.write(value);
    }

    private static void namedIntArray(DataOutputStream out, String name, int[] value) throws IOException {
        out.writeByte(TAG_INT_ARRAY);
        out.writeUTF(name);
        out.writeInt(value.length);
        for (int v : value) {
            out.writeInt(v);
        }
    }

    private static void namedListHeader(DataOutputStream out, String name, int childType, int length) throws IOException {
        out.writeByte(TAG_LIST);
        out.writeUTF(name);
        out.writeByte(childType);
        out.writeInt(length);
    }

    private static void end(DataOutputStream out) throws IOException {
        out.writeByte(TAG_END);
    }
}
