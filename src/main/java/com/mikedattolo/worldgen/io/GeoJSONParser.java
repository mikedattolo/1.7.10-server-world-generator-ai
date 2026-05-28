package com.mikedattolo.worldgen.io;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeoJSONParser {
    private static final Pattern FEATURE_PATTERN = Pattern.compile("\\\"type\\\"\\s*:\\s*\\\"Feature\\\"");

    public int countFeatures(String geoJsonContent) {
        Matcher matcher = FEATURE_PATTERN.matcher(geoJsonContent);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
