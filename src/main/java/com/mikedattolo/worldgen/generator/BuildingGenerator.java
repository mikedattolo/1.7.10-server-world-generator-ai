package com.mikedattolo.worldgen.generator;

public class BuildingGenerator {
    public String classify(String osmType, double footprintArea) {
        if (osmType == null) {
            return footprintArea < 80 ? "house" : "commercial";
        }
        String normalized = osmType.toLowerCase();
        if (normalized.contains("hospital")) return "hospital";
        if (normalized.contains("school")) return "school";
        if (normalized.contains("industrial") || normalized.contains("warehouse")) return "industrial";
        if (normalized.contains("apartment")) return "apartment";
        if (normalized.contains("garage")) return "garage";
        if (normalized.contains("church")) return "church";
        if (normalized.contains("house") || normalized.contains("residential")) return "house";
        return footprintArea < 120 ? "residential" : "commercial";
    }
}
