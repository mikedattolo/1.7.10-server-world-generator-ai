package com.mikedattolo.worldbuilder;

import com.mikedattolo.worldbuilder.cli.CLIParser;
import com.mikedattolo.worldbuilder.gui.WorldBuilderGuiApp;
import com.mikedattolo.worldbuilder.model.CLIOptions;

public class RealWorldMapBuilderApp {
    /**
     * External preprocessor entrypoint. This intentionally runs outside Minecraft and writes
     * normalized world-generation artifacts that the in-game Forge 1.7.10 runtime can consume.
     */
    public static void main(String[] args) throws Exception {
        CLIOptions options = CLIParser.parse(args);
        if (options.helpRequested) {
            printUsage();
            return;
        }
        if (options.launchGui) {
            WorldBuilderGuiApp.main(new String[0]);
            return;
        }

        WorldBuilderService.GenerationResult result = new WorldBuilderService().generate(options);
        System.out.println("Generated project: " + result.metadata.projectName);
        System.out.println("Mode: " + result.metadata.mode);
        System.out.println("Real-world dimensions (degrees): " + WorldBuilderService.dimensions(result.metadata));
        System.out.println("World size: " + result.metadata.worldSize + "x" + result.metadata.worldSize);
        System.out.println("Scale ratio (blocks/degree): " + WorldBuilderService.scaleRatio(result.metadata));
        System.out.println("Estimated memory usage (rough MB): " + WorldBuilderService.estimateMemoryMb(result.metadata));
        System.out.println("Output: " + result.outputPath);
    }

    private static void printUsage() {
        System.out.println("RealWorldMapBuilder usage:");
        System.out.println("  --gui                              Launch GUI mode");
        System.out.println("  --help                             Show this help");
        System.out.println("  --prompt <text> [--style <name>] [--output <path>]");
        System.out.println("  --bbox <lat1,lon1,lat2,lon2> [--size <n>] [--verticalScale <v>] [--style <name>] [--output <path>]");
        System.out.println("  --address <text> [--radius <meters>] [--size <n>] [--verticalScale <v>] [--style <name>] [--output <path>]");
        System.out.println("If no arguments are provided, GUI mode is launched.");
    }
}
