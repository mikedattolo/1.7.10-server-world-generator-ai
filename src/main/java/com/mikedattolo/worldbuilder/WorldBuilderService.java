package com.mikedattolo.worldbuilder;

import com.mikedattolo.worldbuilder.dem.AddressResolver;
import com.mikedattolo.worldbuilder.dem.BoundingBox;
import com.mikedattolo.worldbuilder.export.ExportService;
import com.mikedattolo.worldbuilder.model.CLIOptions;
import com.mikedattolo.worldbuilder.model.GenerationMode;
import com.mikedattolo.worldbuilder.model.GenerationPlan;
import com.mikedattolo.worldbuilder.model.ProjectMetadata;
import com.mikedattolo.worldbuilder.prompt.PromptParser;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class WorldBuilderService {
    public GenerationResult generate(CLIOptions options) throws IOException {
        ProjectMetadata metadata = buildMetadata(options);
        GenerationPlan plan = buildPlan(options);
        Path outputPath = Paths.get(options.output);
        new ExportService().export(outputPath, metadata, plan);
        return new GenerationResult(metadata, plan, outputPath.toAbsolutePath());
    }

    public static ProjectMetadata buildMetadata(CLIOptions options) {
        BoundingBox bbox;
        if (options.mode == GenerationMode.PROMPT) {
            bbox = new BoundingBox(0.0, 0.0, 1.0, 1.0);
        } else if (options.bbox != null) {
            bbox = BoundingBox.parse(options.bbox);
        } else {
            bbox = AddressResolver.resolveToBoundingBox(options.address, options.radius == null ? 1000 : options.radius);
        }

        ProjectMetadata m = new ProjectMetadata();
        m.projectName = options.projectName;
        m.mode = options.mode;
        m.worldSize = options.worldSize;
        m.verticalScale = options.verticalScale;
        m.style = options.style;
        m.bbox = bbox.asMap();
        m.center = bbox.center();
        m.estimatedFeatures = estimateFeatures(bbox);
        return m;
    }

    public static GenerationPlan buildPlan(CLIOptions options) {
        if (options.mode == GenerationMode.PROMPT) {
            return new PromptParser().parse(options.prompt, options.style);
        }
        GenerationPlan p = new GenerationPlan();
        p.theme = "real-world";
        p.terrain = "dem-driven";
        p.roads = "osm-geometry";
        p.vegetation = "land-cover";
        p.style = options.style;
        p.structures.add("houses");
        p.structures.add("commercial");
        p.specialFeatures.add("source-aligned placement");
        return p;
    }

    public static Map<String, Integer> estimateFeatures(BoundingBox bbox) {
        double latSpan = Math.abs(bbox.maxLat - bbox.minLat);
        double lonSpan = Math.abs(bbox.maxLon - bbox.minLon);
        int complexity = Math.max(1, (int) (latSpan * lonSpan * 10000000));
        Map<String, Integer> m = new LinkedHashMap<String, Integer>();
        m.put("roads", complexity * 8);
        m.put("buildings", complexity * 15);
        m.put("water", complexity * 3);
        m.put("vegetation", complexity * 25);
        m.put("landuse", complexity * 6);
        return m;
    }

    public static double scaleRatio(ProjectMetadata metadata) {
        double latSpan = Math.abs(metadata.bbox.get("maxLat") - metadata.bbox.get("minLat"));
        return metadata.worldSize / Math.max(latSpan, 0.0001);
    }

    public static int estimateMemoryMb(ProjectMetadata metadata) {
        long cells = (long) metadata.worldSize * metadata.worldSize;
        return (int) Math.max(64, cells / (1024 * 8));
    }

    public static String dimensions(ProjectMetadata metadata) {
        double latSpan = Math.abs(metadata.bbox.get("maxLat") - metadata.bbox.get("minLat"));
        double lonSpan = Math.abs(metadata.bbox.get("maxLon") - metadata.bbox.get("minLon"));
        return String.format("%.6f x %.6f", latSpan, lonSpan);
    }

    public static final class GenerationResult {
        public final ProjectMetadata metadata;
        public final GenerationPlan plan;
        public final Path outputPath;

        public GenerationResult(ProjectMetadata metadata, GenerationPlan plan, Path outputPath) {
            this.metadata = metadata;
            this.plan = plan;
            this.outputPath = outputPath;
        }
    }
}