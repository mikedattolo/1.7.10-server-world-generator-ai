package com.mikedattolo.worldgen.io;

public class ElevationLoader {
    public int[] loadSamples(String elevationJson) {
        int start = elevationJson.indexOf('[');
        int end = elevationJson.indexOf(']');
        if (start < 0 || end <= start) {
            return new int[0];
        }
        String[] tokens = elevationJson.substring(start + 1, end).split(",");
        int[] values = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            values[i] = Integer.parseInt(tokens[i].trim());
        }
        return values;
    }
}
