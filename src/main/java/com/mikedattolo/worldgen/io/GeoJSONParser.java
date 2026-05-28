package com.mikedattolo.worldgen.io;

public class GeoJSONParser {
    public int countFeatures(String geoJsonContent) {
        int count = 0;
        int idx = 0;
        while ((idx = geoJsonContent.indexOf("\"type\": \"Feature\"", idx)) >= 0) {
            count++;
            idx += 10;
        }
        return count;
    }
}
