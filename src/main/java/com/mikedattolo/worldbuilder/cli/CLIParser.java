package com.mikedattolo.worldbuilder.cli;

import com.mikedattolo.worldbuilder.model.CLIOptions;
import com.mikedattolo.worldbuilder.model.GenerationMode;

public final class CLIParser {
    private CLIParser() {
    }

    public static CLIOptions parse(String[] args) {
        CLIOptions options = new CLIOptions();
        if (args == null || args.length == 0) {
            options.launchGui = true;
            return options;
        }

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (!arg.startsWith("--")) {
                continue;
            }
            if ("--help".equals(arg) || "--gui".equals(arg)) {
                options.helpRequested = "--help".equals(arg);
                options.launchGui = "--gui".equals(arg);
                continue;
            }

            String value = (i + 1) < args.length ? args[i + 1] : null;
            if (value == null || value.startsWith("--")) {
                throw new IllegalArgumentException("Missing value for " + arg);
            }
            i++;

            if ("--mode".equals(arg)) {
                options.mode = GenerationMode.valueOf(value.toUpperCase());
            } else if ("--bbox".equals(arg)) {
                options.bbox = value;
            } else if ("--address".equals(arg)) {
                options.address = value;
            } else if ("--radius".equals(arg)) {
                options.radius = Integer.valueOf(value);
            } else if ("--size".equals(arg)) {
                options.worldSize = Integer.parseInt(value);
            } else if ("--verticalScale".equals(arg)) {
                options.verticalScale = Double.parseDouble(value);
            } else if ("--style".equals(arg)) {
                options.style = value;
            } else if ("--output".equals(arg)) {
                options.output = value;
            } else if ("--prompt".equals(arg)) {
                options.prompt = value;
            } else if ("--projectName".equals(arg)) {
                options.projectName = value;
            } else {
                throw new IllegalArgumentException("Unknown argument " + arg);
            }
        }

        if (options.helpRequested || options.launchGui) {
            return options;
        }

        if (options.mode == null) {
            options.mode = options.prompt != null ? GenerationMode.PROMPT : GenerationMode.DEM;
        }
        if (options.mode == GenerationMode.PROMPT && (options.prompt == null || options.prompt.trim().isEmpty())) {
            throw new IllegalArgumentException("PROMPT mode requires --prompt");
        }
        if (options.mode == GenerationMode.DEM && options.bbox == null && options.address == null) {
            throw new IllegalArgumentException("DEM mode requires --bbox or --address");
        }
        if (options.worldSize <= 0) {
            throw new IllegalArgumentException("--size must be positive");
        }
        return options;
    }
}
