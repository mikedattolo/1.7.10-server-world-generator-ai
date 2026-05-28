package com.mikedattolo.worldbuilder.util;

import com.mikedattolo.worldbuilder.model.GenerationPlan;
import com.mikedattolo.worldbuilder.model.ProjectMetadata;

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

    public static String placeholderGeoJson(String layerName) {
        return "{\n" +
                "  \"type\": \"FeatureCollection\",\n" +
                "  \"name\": \"" + esc(layerName) + "\",\n" +
                "  \"features\": [\n" +
                "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"properties\": {\"classification\": \"placeholder\"},\n" +
                "      \"geometry\": {\"type\": \"LineString\", \"coordinates\": [[0,0],[64,64]]}\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";
    }

    public static String placeholderElevationJson(int size, double verticalScale) {
        return "{\n" +
                "  \"gridSize\": " + size + ",\n" +
                "  \"verticalScale\": " + verticalScale + ",\n" +
                "  \"samples\": [0, 1, 2, 3, 4]\n" +
                "}\n";
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
