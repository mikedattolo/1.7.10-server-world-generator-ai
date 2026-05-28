package com.mikedattolo.worldbuilder.model;

public class CLIOptions {
    public boolean helpRequested;
    public boolean launchGui;
    public GenerationMode mode;
    public String bbox;
    public String address;
    public Integer radius;
    public int worldSize = 2048;
    public double verticalScale = 1.0;
    public String style = "realistic";
    public String output = "config/worldgen/realworld";
    public String prompt;
    public String projectName = "RealWorldProject";
}
