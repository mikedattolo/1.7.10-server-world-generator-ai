package com.mikedattolo.worldgen.io;

public class GeoJSONParser {
    public int countFeatures(String geoJsonContent) {
        int count = 0;
        int idx = 0;
        String marker = "\"type\": \"Feature\",";
        while ((idx = geoJsonContent.indexOf(marker, idx)) >= 0) {
            count++;
            idx += marker.length();
        }
        return count;
    }
}
