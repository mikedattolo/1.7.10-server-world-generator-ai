package com.mikedattolo.worldbuilder.export;

import com.mikedattolo.worldbuilder.minecraft.MinecraftWorldExporter;
import com.mikedattolo.worldbuilder.model.GenerationPlan;
import com.mikedattolo.worldbuilder.model.ProjectMetadata;
import com.mikedattolo.worldbuilder.util.JsonUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExportService {
    public void export(Path outputDir, ProjectMetadata metadata, GenerationPlan plan) throws IOException {
        Files.createDirectories(outputDir);
        write(outputDir.resolve("project.json"), JsonUtil.toJson(metadata));
        write(outputDir.resolve("generation_plan.json"), JsonUtil.toJson(plan));
        write(outputDir.resolve("elevation.json"), JsonUtil.syntheticElevationJson(metadata));
        write(outputDir.resolve("roads.geojson"), JsonUtil.syntheticRoadsGeoJson(metadata, plan));
        write(outputDir.resolve("buildings.geojson"), JsonUtil.syntheticBuildingsGeoJson(metadata, plan));
        write(outputDir.resolve("water.geojson"), JsonUtil.syntheticWaterGeoJson(metadata, plan));
        write(outputDir.resolve("vegetation.geojson"), JsonUtil.syntheticVegetationGeoJson(metadata, plan));
        write(outputDir.resolve("landuse.geojson"), JsonUtil.syntheticLanduseGeoJson(metadata, plan));
        new MinecraftWorldExporter().export(outputDir.resolve("minecraft_world"), metadata);
    }

    private void write(Path path, String content) throws IOException {
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }
}
