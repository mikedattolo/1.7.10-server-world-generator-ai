package com.mikedattolo.worldgen.io;

import java.util.HashMap;
import java.util.Map;

public class PreparedWorldData {
    public final Map<String, String> rawFiles = new HashMap<String, String>();
    public final Map<String, Integer> featureCounts = new HashMap<String, Integer>();
    public int worldSize;
    public String style;
    public String mode;
}
