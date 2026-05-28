package com.mikedattolo.worldgen.io;

import com.mikedattolo.worldbuilder.export.ExportService;
import com.mikedattolo.worldbuilder.model.GenerationMode;
import com.mikedattolo.worldbuilder.model.GenerationPlan;
import com.mikedattolo.worldbuilder.model.ProjectMetadata;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreparedWorldDataLoaderTest {
    @Test
    void loadsExportedArtifactsAndCountsFeatures() throws Exception {
        Path dir = Files.createTempDirectory("prepared-data-loader-test");

        ProjectMetadata metadata = new ProjectMetadata();
        metadata.projectName = "loader-test";
        metadata.mode = GenerationMode.DEM;
        metadata.worldSize = 512;
        metadata.verticalScale = 1.4;
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
        plan.terrain = "dem-driven";
        plan.roads = "osm";
        plan.vegetation = "land-cover";
        plan.style = "apocalypse";
        plan.structures.add("houses");
        plan.structures.add("commercial");

        new ExportService().export(dir, metadata, plan);

        PreparedWorldData loaded = new PreparedWorldDataLoader().load(dir);

        assertEquals(8, loaded.rawFiles.size());
        assertEquals(512, loaded.worldSize);
        assertEquals("apocalypse", loaded.style);

        assertEquals(5, loaded.featureCounts.get("roads.geojson").intValue());
        assertEquals(6, loaded.featureCounts.get("buildings.geojson").intValue());
        assertEquals(2, loaded.featureCounts.get("water.geojson").intValue());
        assertEquals(8, loaded.featureCounts.get("vegetation.geojson").intValue());
        assertEquals(3, loaded.featureCounts.get("landuse.geojson").intValue());

        assertTrue(loaded.rawFiles.get("roads.geojson").contains("\"type\": \"FeatureCollection\""));
        assertTrue(loaded.rawFiles.get("roads.geojson").contains("\"type\": \"Feature\","));
    }
}
