package com.mikedattolo.worldbuilder;

import com.mikedattolo.worldbuilder.cli.CLIParser;
import com.mikedattolo.worldbuilder.model.CLIOptions;
import com.mikedattolo.worldbuilder.model.GenerationMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CLIParserTest {
    @Test
    void launchesGuiWhenNoArgumentsProvided() {
        CLIOptions options = CLIParser.parse(new String[]{});
        assertTrue(options.launchGui);
    }

    @Test
    void parsesHelpAndGuiFlags() {
        CLIOptions help = CLIParser.parse(new String[]{"--help"});
        CLIOptions gui = CLIParser.parse(new String[]{"--gui"});
        assertTrue(help.helpRequested);
        assertTrue(gui.launchGui);
    }

    @Test
    void parsesPromptMode() {
        CLIOptions options = CLIParser.parse(new String[]{"--prompt", "abandoned town", "--style", "apocalypse"});
        assertEquals(GenerationMode.PROMPT, options.mode);
        assertEquals("apocalypse", options.style);
    }

    @Test
    void validatesDemInputs() {
        assertThrows(IllegalArgumentException.class, () -> CLIParser.parse(new String[]{"--mode", "DEM"}));
    }
}
