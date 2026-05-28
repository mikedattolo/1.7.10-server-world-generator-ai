package com.mikedattolo.worldbuilder.dem;

import java.util.LinkedHashMap;
import java.util.Map;

public class BoundingBox {
    public final double minLat;
    public final double minLon;
    public final double maxLat;
    public final double maxLon;

    public BoundingBox(double minLat, double minLon, double maxLat, double maxLon) {
        this.minLat = minLat;
        this.minLon = minLon;
        this.maxLat = maxLat;
        this.maxLon = maxLon;
    }

    public static BoundingBox parse(String bbox) {
        String[] parts = bbox.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid bbox format, expected lat1,lon1,lat2,lon2");
        }
        return new BoundingBox(
                Double.parseDouble(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3])
        );
    }

    public Map<String, Double> asMap() {
        Map<String, Double> m = new LinkedHashMap<String, Double>();
        m.put("minLat", minLat);
        m.put("minLon", minLon);
        m.put("maxLat", maxLat);
        m.put("maxLon", maxLon);
        return m;
    }

    public Map<String, Double> center() {
        Map<String, Double> m = new LinkedHashMap<String, Double>();
        m.put("lat", (minLat + maxLat) / 2.0);
        m.put("lon", (minLon + maxLon) / 2.0);
        return m;
    }
}
