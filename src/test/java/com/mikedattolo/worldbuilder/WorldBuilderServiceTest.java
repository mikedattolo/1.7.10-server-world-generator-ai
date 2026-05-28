package com.mikedattolo.worldbuilder;

import com.mikedattolo.worldbuilder.model.CLIOptions;
import com.mikedattolo.worldbuilder.model.GenerationMode;
import com.mikedattolo.worldbuilder.model.GenerationPlan;
import com.mikedattolo.worldbuilder.model.ProjectMetadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldBuilderServiceTest {
    @Test
    void buildsPromptPlanWithRicherSignals() {
        CLIOptions options = new CLIOptions();
        options.mode = GenerationMode.PROMPT;
        options.prompt = "post-apocalyptic city with hospital and rail line";
        options.style = "apocalypse";

        GenerationPlan plan = WorldBuilderService.buildPlan(options);
        assertEquals("abandoned", plan.theme);
        assertTrue(plan.structures.contains("hospital"));
        assertTrue(plan.structures.contains("city"));
        assertTrue(plan.specialFeatures.contains("rail line"));
    }

    @Test
    void computesMetadataAndRatiosForDemMode() {
        CLIOptions options = new CLIOptions();
        options.mode = GenerationMode.DEM;
        options.bbox = "40.123,-74.123,40.130,-74.115";
        options.worldSize = 1024;
        options.verticalScale = 1.5;
        options.style = "realistic";

        ProjectMetadata metadata = WorldBuilderService.buildMetadata(options);
        assertEquals(1024, metadata.worldSize);
        assertEquals("realistic", metadata.style);
        assertTrue(metadata.estimatedFeatures.get("roads") > 0);
        assertTrue(WorldBuilderService.scaleRatio(metadata) > 0.0d);
    }
}
