package com.mikedattolo.worldbuilder.util;

import com.mikedattolo.worldbuilder.model.GenerationPlan;
import com.mikedattolo.worldbuilder.model.ProjectMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class JsonUtil {
    private JsonUtil() {
    }

    public static String toJson(ProjectMetadata m) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        kv(sb, "projectName", m.projectName, true);
        kv(sb, "mode", String.valueOf(m.mode), true);
        kv(sb, "worldSize", m.worldSize, false);
        kv(sb, "verticalScale", m.verticalScale, false);
        kv(sb, "style", m.style, true);
        map(sb, "bbox", m.bbox);
        sb.append(",\n");
        map(sb, "center", m.center);
        sb.append(",\n");
        kv(sb, "generatedTimestamp", m.generatedTimestamp, true);
        sb.append(",\n");
        intMap(sb, "estimatedFeatures", m.estimatedFeatures);
        sb.append(",\n");
        stringArray(sb, "dataSources", m.dataSources);
        sb.append("\n}\n");
        return sb.toString();
    }

    public static String toJson(GenerationPlan p) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        kv(sb, "theme", p.theme, true);
        kv(sb, "terrain", p.terrain, true);
        kv(sb, "roads", p.roads, true);
        kv(sb, "vegetation", p.vegetation, true);
        kv(sb, "style", p.style, true);
        list(sb, "structures", p.structures.toArray(new String[0]));
        sb.append(",\n");
        list(sb, "specialFeatures", p.specialFeatures.toArray(new String[0]));
        sb.append("\n}\n");
        return sb.toString();
    }

    public static String syntheticRoadsGeoJson(ProjectMetadata metadata, GenerationPlan plan) {
        int count = featureCount(metadata, "roads", 5, 24);
        List<String> features = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            int laneBias = 2 + (i % 3);
            double y = coordinate(i, count, metadata.worldSize, 0.08, 0.84);
            double x1 = coordinate(i * 3 + 1, count + 2, metadata.worldSize, 0.05, 0.25);
            double x2 = coordinate(i * 5 + 2, count + 3, metadata.worldSize, 0.72, 0.95);
            String coordinates = "[[" + fmt(x1) + "," + fmt(y) + "],[" + fmt((x1 + x2) / 2.0) + "," + fmt(y + roadCurve(i, metadata.worldSize)) + "],[" + fmt(x2) + "," + fmt(y) + "]]";
            features.add(feature("collector-road", plan.style, laneBias + "-lane", "LineString", coordinates));
        }
        return featureCollection("roads", features);
    }

    public static String syntheticBuildingsGeoJson(ProjectMetadata metadata, GenerationPlan plan) {
        int count = featureCount(metadata, "buildings", 6, 32);
        List<String> features = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            double x = coordinate(i * 2 + 1, count, metadata.worldSize, 0.08, 0.88);
            double y = coordinate(i * 3 + 2, count + 1, metadata.worldSize, 0.08, 0.88);
            double width = Math.max(10.0, metadata.worldSize * 0.018 + (i % 4) * 3.0);
            double height = Math.max(10.0, metadata.worldSize * 0.014 + (i % 5) * 2.0);
            String classification = pick(plan.structures, i, "mixed-use");
            features.add(feature(classification, plan.style, String.valueOf(1 + (i % 4)), "Polygon", rectangle(x, y, width, height)));
        }
        return featureCollection("buildings", features);
    }

    public static String syntheticWaterGeoJson(ProjectMetadata metadata, GenerationPlan plan) {
        int count = featureCount(metadata, "water", 2, 8);
        List<String> features = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            double x = coordinate(i + 1, count, metadata.worldSize, 0.12, 0.72);
            double y = coordinate(i * 2 + 1, count + 1, metadata.worldSize, 0.12, 0.72);
            double width = Math.max(24.0, metadata.worldSize * 0.08 - i * 4.0);
            double height = Math.max(18.0, metadata.worldSize * 0.05 + i * 3.0);
            features.add(feature("water-body", plan.style, "surface", "Polygon", rectangle(x, y, width, height)));
        }
        return featureCollection("water", features);
    }

    public static String syntheticVegetationGeoJson(ProjectMetadata metadata, GenerationPlan plan) {
        int count = featureCount(metadata, "vegetation", 4, 20);
        List<String> features = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            double x = coordinate(i + 2, count + 2, metadata.worldSize, 0.05, 0.9);
            double y = coordinate(i * 4 + 1, count + 3, metadata.worldSize, 0.05, 0.9);
            double width = Math.max(14.0, metadata.worldSize * 0.04 + (i % 3) * 6.0);
            double height = Math.max(14.0, metadata.worldSize * 0.035 + (i % 5) * 5.0);
            String classification = plan.vegetation == null || plan.vegetation.isEmpty() ? "greenery" : plan.vegetation;
            features.add(feature(classification, plan.style, "cluster", "Polygon", rectangle(x, y, width, height)));
        }
        return featureCollection("vegetation", features);
    }

    public static String syntheticLanduseGeoJson(ProjectMetadata metadata, GenerationPlan plan) {
        int count = featureCount(metadata, "landuse", 3, 12);
        List<String> features = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            double x = coordinate(i + 1, count + 1, metadata.worldSize, 0.04, 0.84);
            double y = coordinate(i + 3, count + 3, metadata.worldSize, 0.04, 0.84);
            double width = Math.max(28.0, metadata.worldSize * 0.11 - i * 3.0);
            double height = Math.max(28.0, metadata.worldSize * 0.09 - i * 2.0);
            String classification = i % 2 == 0 ? plan.theme : "transition-zone";
            features.add(feature(classification, plan.style, "parcel", "Polygon", rectangle(x, y, width, height)));
        }
        return featureCollection("landuse", features);
    }

    public static String syntheticElevationJson(ProjectMetadata metadata) {
        int resolution = Math.max(8, Math.min(32, metadata.worldSize / 64));
        int[] samples = new int[resolution * resolution];
        double amplitude = Math.max(4.0, 18.0 * metadata.verticalScale);
        for (int row = 0; row < resolution; row++) {
            for (int col = 0; col < resolution; col++) {
                double ridge = Math.sin((row + 1) * 0.45) * amplitude;
                double basin = Math.cos((col + 2) * 0.32) * (amplitude * 0.6);
                double drift = ((row - col) * metadata.verticalScale) * 0.35;
                samples[row * resolution + col] = (int) Math.round(ridge + basin + drift);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        kv(sb, "gridSize", metadata.worldSize, false);
        kv(sb, "verticalScale", metadata.verticalScale, false);
        kv(sb, "sampleResolution", resolution, false);
        sb.append(",\n");
        sb.append("  \"samples\": [");
        for (int i = 0; i < samples.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(samples[i]);
        }
        sb.append("]\n}");
        sb.append('\n');
        return sb.toString();
    }

    private static int featureCount(ProjectMetadata metadata, String key, int min, int max) {
        Integer estimate = metadata.estimatedFeatures.get(key);
        if (estimate == null) {
            return min;
        }
        return Math.max(min, Math.min(max, Math.max(1, estimate / 12)));
    }

    private static double coordinate(int index, int total, int worldSize, double startFactor, double endFactor) {
        double span = Math.max(1.0, worldSize * (endFactor - startFactor));
        double step = span / Math.max(1, total);
        return worldSize * startFactor + (index % Math.max(1, total)) * step;
    }

    private static double roadCurve(int index, int worldSize) {
        return ((index % 5) - 2) * Math.max(4.0, worldSize * 0.01);
    }

    private static String rectangle(double x, double y, double width, double height) {
        double maxX = x + width;
        double maxY = y + height;
        return "[[[" + fmt(x) + "," + fmt(y) + "],[" + fmt(maxX) + "," + fmt(y) + "],[" + fmt(maxX) + "," + fmt(maxY) + "],[" + fmt(x) + "," + fmt(maxY) + "],[" + fmt(x) + "," + fmt(y) + "]]]";
    }

    private static String pick(List<String> values, int index, String fallback) {
        if (values == null || values.isEmpty()) {
            return fallback;
        }
        return values.get(index % values.size());
    }

    private static String featureCollection(String layerName, List<String> features) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"type\": \"FeatureCollection\",\n");
        sb.append("  \"name\": \"").append(esc(layerName)).append("\",\n");
        sb.append("  \"features\": [\n");
        for (int i = 0; i < features.size(); i++) {
            if (i > 0) {
                sb.append(",\n");
            }
            sb.append(features.get(i));
        }
        sb.append("\n  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static String feature(String classification, String style, String variant, String geometryType, String coordinates) {
        return "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"properties\": {\"classification\": \"" + esc(classification) + "\", \"style\": \"" + esc(style) + "\", \"variant\": \"" + esc(variant) + "\"},\n" +
                "      \"geometry\": {\"type\": \"" + geometryType + "\", \"coordinates\": " + coordinates + "}\n" +
                "    }";
    }

    private static String fmt(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private static void map(StringBuilder sb, String key, Map<String, Double> values) {
        sb.append("  \"").append(esc(key)).append("\": {");
        boolean first = true;
        for (Map.Entry<String, Double> e : values.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append("\"").append(esc(e.getKey())).append("\": ").append(e.getValue());
            first = false;
        }
        sb.append("}");
    }

    private static void intMap(StringBuilder sb, String key, Map<String, Integer> values) {
        sb.append("  \"").append(esc(key)).append("\": {");
        boolean first = true;
        for (Map.Entry<String, Integer> e : values.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append("\"").append(esc(e.getKey())).append("\": ").append(e.getValue());
            first = false;
        }
        sb.append("}");
    }

    private static void list(StringBuilder sb, String key, String[] values) {
        sb.append("  \"").append(esc(key)).append("\": [");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("\"").append(esc(values[i])).append("\"");
        }
        sb.append("]");
    }

    private static void stringArray(StringBuilder sb, String key, String[] values) {
        list(sb, key, values);
    }

    private static void kv(StringBuilder sb, String key, Object value, boolean quote) {
        if (sb.charAt(sb.length() - 1) != '\n') {
            sb.append(",\n");
        }
        sb.append("  \"").append(esc(key)).append("\": ");
        if (quote) {
            sb.append("\"").append(esc(String.valueOf(value))).append("\"");
        } else {
            sb.append(value);
        }
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
