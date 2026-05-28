package com.mikedattolo.worldbuilder;

import com.mikedattolo.worldbuilder.model.GenerationPlan;
import com.mikedattolo.worldbuilder.prompt.PromptParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromptParserTest {
    @Test
    void infersDenseApocalypseUrbanSignals() {
        GenerationPlan plan = new PromptParser().parse(
                "Generate a post-apocalyptic downtown with a hospital, bridge, rail line and dense woods",
                "apocalypse");

        assertEquals("abandoned", plan.theme);
        assertEquals("dense", plan.vegetation);
        assertTrue(plan.structures.contains("hospital"));
        assertTrue(plan.structures.contains("city"));
        assertTrue(plan.specialFeatures.contains("bridge crossings"));
        assertTrue(plan.specialFeatures.contains("rail line"));
        assertTrue(plan.specialFeatures.contains("wrecked cars"));
    }

    @Test
    void defaultsToHousesWhenNoStructuresMentioned() {
        GenerationPlan plan = new PromptParser().parse(
                "quiet rolling terrain with mist",
                "realistic");

        assertEquals("rolling hills", plan.terrain);
        assertEquals("moderate", plan.vegetation);
        assertTrue(plan.structures.contains("houses"));
        assertTrue(plan.specialFeatures.contains("low-visibility pockets"));
    }
}