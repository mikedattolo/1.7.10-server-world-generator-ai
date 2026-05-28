package com.mikedattolo.worldbuilder;

import com.mikedattolo.worldbuilder.export.ExportService;
import com.mikedattolo.worldbuilder.model.GenerationMode;
import com.mikedattolo.worldbuilder.model.GenerationPlan;
import com.mikedattolo.worldbuilder.model.ProjectMetadata;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;

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
        metadata.bbox = new LinkedHashMap<>();
        metadata.bbox.put("minLat", 1.0);
        metadata.bbox.put("minLon", 2.0);
        metadata.bbox.put("maxLat", 3.0);
        metadata.bbox.put("maxLon", 4.0);
        metadata.center = new LinkedHashMap<>();
        metadata.center.put("lat", 2.0);
        metadata.center.put("lon", 3.0);

        GenerationPlan plan = new GenerationPlan();
        plan.theme = "real-world";
        plan.terrain = "dem";
        plan.roads = "osm";
        plan.vegetation = "land-cover";
        plan.style = "apocalypse";

        new ExportService().export(dir, metadata, plan);

        assertTrue(Files.exists(dir.resolve("project.json")));
        assertTrue(Files.exists(dir.resolve("generation_plan.json")));
        assertTrue(Files.exists(dir.resolve("roads.geojson")));
        assertTrue(new String(Files.readAllBytes(dir.resolve("project.json")), StandardCharsets.UTF_8).contains("\"projectName\": \"test\""));
        assertTrue(new String(Files.readAllBytes(dir.resolve("generation_plan.json")), StandardCharsets.UTF_8).contains("\"theme\": \"real-world\""));
    }
}
