package com.mikedattolo.worldbuilder;

import com.mikedattolo.worldbuilder.export.ExportService;
import com.mikedattolo.worldbuilder.model.GenerationMode;
import com.mikedattolo.worldbuilder.model.GenerationPlan;
import com.mikedattolo.worldbuilder.model.ProjectMetadata;
import com.mikedattolo.worldgen.io.ElevationLoader;
import com.mikedattolo.worldgen.io.GeoJSONParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportServiceTest {
    @Test
    void writesExpectedFiles() throws Exception {
        Path dir = Files.createTempDirectory("builder-test");

        ProjectMetadata metadata = new ProjectMetadata();
        metadata.projectName = "test";
        metadata.mode = GenerationMode.DEM;
        metadata.worldSize = 512;
        metadata.verticalScale = 1.5;
        metadata.style = "apocalypse";
        metadata.bbox = new LinkedHashMap<String, Double>();
        metadata.bbox.put("minLat", 1.0);
        metadata.bbox.put("minLon", 2.0);
        metadata.bbox.put("maxLat", 3.0);
        metadata.bbox.put("maxLon", 4.0);
        metadata.center = new LinkedHashMap<String, Double>();
        metadata.center.put("lat", 2.0);
        metadata.center.put("lon", 3.0);
        metadata.estimatedFeatures.put("roads", 48);
        metadata.estimatedFeatures.put("buildings", 72);
        metadata.estimatedFeatures.put("water", 18);
        metadata.estimatedFeatures.put("vegetation", 96);
        metadata.estimatedFeatures.put("landuse", 24);

        GenerationPlan plan = new GenerationPlan();
        plan.theme = "real-world";
        plan.terrain = "dem";
        plan.roads = "osm";
        plan.vegetation = "land-cover";
        plan.style = "apocalypse";
        plan.structures.add("houses");
        plan.structures.add("commercial");

        new ExportService().export(dir, metadata, plan);

        assertTrue(Files.exists(dir.resolve("project.json")));
        assertTrue(Files.exists(dir.resolve("generation_plan.json")));
        assertTrue(Files.exists(dir.resolve("elevation.json")));
        assertTrue(Files.exists(dir.resolve("roads.geojson")));
        assertTrue(Files.exists(dir.resolve("minecraft_world/session.lock")));
        assertTrue(Files.exists(dir.resolve("minecraft_world/level.dat")));
        assertTrue(Files.exists(dir.resolve("minecraft_world/region/r.0.0.mca")));
        assertTrue(Files.size(dir.resolve("minecraft_world/region/r.0.0.mca")) > 8192L);

        String roads = new String(Files.readAllBytes(dir.resolve("roads.geojson")));
        String buildings = new String(Files.readAllBytes(dir.resolve("buildings.geojson")));
        String elevation = new String(Files.readAllBytes(dir.resolve("elevation.json")));

        GeoJSONParser parser = new GeoJSONParser();
        assertEquals(5, parser.countFeatures(roads));
        assertEquals(6, parser.countFeatures(buildings));
        assertFalse(roads.contains("placeholder"));
        assertTrue(roads.contains("collector-road"));
        assertTrue(buildings.contains("houses") || buildings.contains("commercial"));

        int[] samples = new ElevationLoader().loadSamples(elevation);
        assertEquals(64, samples.length);
        assertTrue(elevation.contains("\"sampleResolution\": 8"));
    }
}
