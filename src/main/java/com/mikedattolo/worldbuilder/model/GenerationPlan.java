package com.mikedattolo.worldbuilder.model;

import java.util.ArrayList;
import java.util.List;

public class GenerationPlan {
    public String theme;
    public String terrain;
    public String roads;
    public String vegetation;
    public String style;
    public final List<String> structures = new ArrayList<String>();
    public final List<String> specialFeatures = new ArrayList<String>();
}
