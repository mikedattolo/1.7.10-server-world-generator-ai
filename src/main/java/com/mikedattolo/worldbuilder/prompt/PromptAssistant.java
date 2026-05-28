package com.mikedattolo.worldbuilder.prompt;

public class PromptAssistant {
    public String improvePrompt(String prompt, String style) {
        String base = prompt == null ? "" : prompt.trim();
        if (base.isEmpty()) {
            base = "Generate a playable Minecraft survival region";
        }

        String lower = base.toLowerCase();
        StringBuilder improved = new StringBuilder(base);
        appendIfMissing(improved, lower, "terrain", " with varied terrain, clear landmarks");
        appendIfMissing(improved, lower, "road", ", connected roads and paths");
        appendIfMissing(improved, lower, "water", ", water features that shape exploration");
        appendIfMissing(improved, lower, "building", ", buildings placed in believable districts");

        if ("apocalypse".equalsIgnoreCase(style) && !containsAny(lower, "abandoned", "ruin", "overgrown")) {
            improved.append(", abandoned infrastructure, wrecked roads and overgrown vegetation");
        } else if ("futuristic".equalsIgnoreCase(style) && !containsAny(lower, "district", "tower", "transit")) {
            improved.append(", dense districts, transit corridors and landmark towers");
        } else if ("historic".equalsIgnoreCase(style) && !containsAny(lower, "village", "castle", "market")) {
            improved.append(", old villages, market squares and defensive landmarks");
        }

        improved.append(". Prioritize Minecraft-scale traversal, recognizable biomes and generated structures that match the theme.");
        return improved.toString();
    }

    private static void appendIfMissing(StringBuilder sb, String lower, String token, String addition) {
        if (!lower.contains(token)) {
            sb.append(addition);
        }
    }

    private static boolean containsAny(String text, String... tokens) {
        for (String token : tokens) {
            if (text.contains(token)) {
                return true;
            }
        }
        return false;
    }
}