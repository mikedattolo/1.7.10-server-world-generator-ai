package com.mikedattolo.worldbuilder;

import com.mikedattolo.worldbuilder.prompt.PromptAssistant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PromptAssistantTest {
    @Test
    void improvesShortApocalypsePromptWithWorldDetails() {
        String improved = new PromptAssistant().improvePrompt("small town", "apocalypse");

        assertTrue(improved.contains("varied terrain"));
        assertTrue(improved.contains("connected roads"));
        assertTrue(improved.contains("abandoned infrastructure"));
        assertTrue(improved.contains("Minecraft-scale traversal"));
    }

    @Test
    void providesDefaultWhenPromptIsEmpty() {
        String improved = new PromptAssistant().improvePrompt(" ", "realistic");

        assertTrue(improved.contains("playable Minecraft survival region"));
        assertTrue(improved.contains("water features"));
    }
}