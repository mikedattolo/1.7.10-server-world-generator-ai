package com.mikedattolo.worldbuilder;

import com.mikedattolo.worldbuilder.cli.CLIParser;
import com.mikedattolo.worldbuilder.dem.AddressResolver;
import com.mikedattolo.worldbuilder.dem.BoundingBox;
import com.mikedattolo.worldbuilder.export.ExportService;
import com.mikedattolo.worldbuilder.model.CLIOptions;
import com.mikedattolo.worldbuilder.model.GenerationMode;
import com.mikedattolo.worldbuilder.model.GenerationPlan;
import com.mikedattolo.worldbuilder.model.ProjectMetadata;
import com.mikedattolo.worldbuilder.prompt.PromptParser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class RealWorldMapBuilderApp {
    /**
     * External preprocessor entrypoint. This intentionally runs outside Minecraft and writes
     * normalized world-generation artifacts that the in-game Forge 1.7.10 runtime can consume.
     */
    public static void main(String[] args) throws Exception {
        CLIOptions options = CLIParser.parse(args);
        ProjectMetadata metadata = buildMetadata(options);
        GenerationPlan plan = buildPlan(options);
        new ExportService().export(Paths.get(options.output), metadata, plan);

        System.out.println("Generated project: " + metadata.projectName);
        System.out.println("Mode: " + metadata.mode);
        System.out.println("Real-world dimensions (degrees): " + dimensions(metadata));
        System.out.println("World size: " + metadata.worldSize + "x" + metadata.worldSize);
        System.out.println("Scale ratio (blocks/degree): " + scaleRatio(metadata));
        System.out.println("Estimated memory usage (rough MB): " + estimateMemoryMb(metadata));
        System.out.println("Output: " + Paths.get(options.output).toAbsolutePath());
    }

    private static ProjectMetadata buildMetadata(CLIOptions options) {
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
        m.estimatedFeatures = estimateFeatures(options, bbox);
        return m;
    }

    private static GenerationPlan buildPlan(CLIOptions options) {
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

    private static Map<String, Integer> estimateFeatures(CLIOptions options, BoundingBox bbox) {
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

    private static double scaleRatio(ProjectMetadata metadata) {
        double latSpan = Math.abs(metadata.bbox.get("maxLat") - metadata.bbox.get("minLat"));
        return metadata.worldSize / Math.max(latSpan, 0.0001);
    }

    private static int estimateMemoryMb(ProjectMetadata metadata) {
        long cells = (long) metadata.worldSize * metadata.worldSize;
        return (int) Math.max(64, cells / (1024 * 8));
    }

    private static String dimensions(ProjectMetadata metadata) {
        double latSpan = Math.abs(metadata.bbox.get("maxLat") - metadata.bbox.get("minLat"));
        double lonSpan = Math.abs(metadata.bbox.get("maxLon") - metadata.bbox.get("minLon"));
        return String.format("%.6f x %.6f", latSpan, lonSpan);
    }
}
