package com.mikedattolo.worldbuilder.prompt;

import com.mikedattolo.worldbuilder.model.GenerationPlan;

public class PromptParser {
    public GenerationPlan parse(String prompt, String style) {
        String lower = prompt.toLowerCase();
        GenerationPlan plan = new GenerationPlan();
        plan.style = style;
        plan.theme = containsAny(lower, "abandoned", "apocalypse", "walking dead") ? "abandoned" : "realistic";
        plan.terrain = containsAny(lower, "mountain", "hill", "hills") ? "light hills" : "mixed plains";
        plan.roads = containsAny(lower, "suburban", "city") ? "suburban curved" : "rural";
        plan.vegetation = containsAny(lower, "dense", "forest", "woods") ? "dense" : "moderate";

        addIfMentioned(plan, lower, "houses", "house", "suburb", "neighborhood");
        addIfMentioned(plan, lower, "hospital", "hospital");
        addIfMentioned(plan, lower, "school", "school");
        addIfMentioned(plan, lower, "industrial", "industrial", "warehouse", "factory");
        addIfMentioned(plan, lower, "city", "city", "downtown");

        if (plan.structures.isEmpty()) {
            plan.structures.add("houses");
        }
        if ("abandoned".equals(plan.theme) || "apocalypse".equalsIgnoreCase(style)) {
            plan.specialFeatures.add("wrecked cars");
            plan.specialFeatures.add("barricades");
            plan.specialFeatures.add("overgrown vegetation");
        }
        return plan;
    }

    private static boolean containsAny(String text, String... tokens) {
        for (String token : tokens) {
            if (text.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static void addIfMentioned(GenerationPlan plan, String text, String value, String... tokens) {
        if (containsAny(text, tokens)) {
            plan.structures.add(value);
        }
    }
}
