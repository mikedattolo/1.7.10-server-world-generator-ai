package com.mikedattolo.worldbuilder.dem;

import java.util.Map;

public class BuildingClassifier {
    public String classify(Map<String, String> tags, double footprintArea, String nearbyLandUse, double distanceToPrimaryRoad) {
        String kind = normalize(tags.get("building"));
        String amenity = normalize(tags.get("amenity"));
        String landUse = normalize(nearbyLandUse);

        if ("hospital".equals(amenity) || "hospital".equals(kind)) return "hospital";
        if ("school".equals(amenity) || "school".equals(kind)) return "school";
        if ("church".equals(amenity) || "church".equals(kind)) return "church";
        if ("industrial".equals(kind) || "industrial".equals(landUse)) return "industrial";
        if ("warehouse".equals(kind)) return "warehouse";
        if ("apartments".equals(kind) || "apartment".equals(kind)) return "apartment";
        if ("garage".equals(kind)) return "garage";
        if ("house".equals(kind)) return "house";

        if ("residential".equals(landUse)) return footprintArea < 220 ? "residential" : "apartment";
        if (distanceToPrimaryRoad < 40 || footprintArea > 500) return "commercial";
        return footprintArea < 120 ? "house" : "commercial";
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
