package com.mikedattolo.worldbuilder;

import com.mikedattolo.worldbuilder.cli.CLIParser;
import com.mikedattolo.worldbuilder.model.CLIOptions;
import com.mikedattolo.worldbuilder.model.GenerationMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CLIParserTest {
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

    @Test
    void allowsPromptValuesStartingWithDashes() {
        CLIOptions options = CLIParser.parse(new String[]{"--prompt", "--abandoned town"});
        assertEquals("--abandoned town", options.prompt);
    }
}
