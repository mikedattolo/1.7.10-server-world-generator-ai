package com.mikedattolo.worldbuilder.model;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProjectMetadata {
    public String projectName;
    public GenerationMode mode;
    public int worldSize;
    public double verticalScale;
    public String style;
    public Map<String, Double> bbox = new LinkedHashMap<String, Double>();
    public Map<String, Double> center = new LinkedHashMap<String, Double>();
    public String generatedTimestamp = Instant.now().toString();
    public Map<String, Integer> estimatedFeatures = new LinkedHashMap<String, Integer>();
    public String[] dataSources = new String[] {"DEM_PLACEHOLDER", "OSM_PLACEHOLDER"};
}
