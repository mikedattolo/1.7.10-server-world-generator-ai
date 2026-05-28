package com.mikedattolo.worldgen.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class PreparedWorldDataLoader {
    private static final Logger LOGGER = Logger.getLogger(PreparedWorldDataLoader.class.getName());
    private static final String[] FILES = {
            "project.json", "generation_plan.json", "elevation.json", "roads.geojson", "buildings.geojson",
            "water.geojson", "vegetation.geojson", "landuse.geojson"
    };

    public PreparedWorldData load(Path dir) throws IOException {
        PreparedWorldData data = new PreparedWorldData();
        for (String file : FILES) {
            Path p = dir.resolve(file);
            if (!Files.exists(p)) {
                LOGGER.warning("Missing prepared data file: " + p);
                continue;
            }
            String content = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
            data.rawFiles.put(file, content);
            if (file.endsWith(".geojson")) {
                data.featureCounts.put(file, countFeatures(content));
            }
        }
        String project = data.rawFiles.get("project.json");
        if (project != null) {
            data.worldSize = extractInt(project, "worldSize", 2048);
            data.style = extractString(project, "style", "realistic");
        }
        return data;
    }

    private static int countFeatures(String text) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf("\"type\": \"Feature\"", index)) >= 0) {
            count++;
            index += 10;
        }
        return count;
    }

    private static int extractInt(String json, String key, int defaultValue) {
        String marker = "\"" + key + "\":";
        int idx = json.indexOf(marker);
        if (idx < 0) {
            return defaultValue;
        }
        int start = idx + marker.length();
        int end = start;
        while (end < json.length() && Character.isWhitespace(json.charAt(end))) {
            end++;
        }
        int numStart = end;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        try {
            return Integer.parseInt(json.substring(numStart, end));
        } catch (RuntimeException ex) {
            return defaultValue;
        }
    }

    private static String extractString(String json, String key, String defaultValue) {
        String marker = "\"" + key + "\":";
        int idx = json.indexOf(marker);
        if (idx < 0) {
            return defaultValue;
        }
        int firstQuote = json.indexOf('"', idx + marker.length());
        int secondQuote = json.indexOf('"', firstQuote + 1);
        if (firstQuote < 0 || secondQuote < 0) {
            return defaultValue;
        }
        return json.substring(firstQuote + 1, secondQuote);
    }
}
