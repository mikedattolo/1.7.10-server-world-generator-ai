package com.mikedattolo.worldbuilder.prompt;

import com.mikedattolo.worldbuilder.model.GenerationPlan;

import java.util.LinkedHashSet;
import java.util.Set;

public class PromptParser {
    public GenerationPlan parse(String prompt, String style) {
        String lower = prompt == null ? "" : prompt.toLowerCase();
        Set<String> structures = new LinkedHashSet<String>();
        Set<String> special = new LinkedHashSet<String>();
        GenerationPlan plan = new GenerationPlan();
        plan.style = style;

        plan.theme = inferTheme(lower, style);
        plan.terrain = inferTerrain(lower);
        plan.roads = inferRoads(lower);
        plan.vegetation = inferVegetation(lower);

        addIfMentioned(structures, lower, "houses", "house", "suburb", "neighborhood", "residential");
        addIfMentioned(structures, lower, "hospital", "hospital", "clinic", "medical");
        addIfMentioned(structures, lower, "school", "school", "campus", "university");
        addIfMentioned(structures, lower, "industrial", "industrial", "warehouse", "factory", "plant");
        addIfMentioned(structures, lower, "commercial", "mall", "shop", "market", "retail");
        addIfMentioned(structures, lower, "city", "city", "downtown", "urban", "highrise");
        addIfMentioned(structures, lower, "military", "military", "base", "checkpoint", "outpost");
        addIfMentioned(structures, lower, "farmland", "farm", "farmland", "barn", "fields");

        addIfMentioned(special, lower, "river corridor", "river", "creek", "stream");
        addIfMentioned(special, lower, "bridge crossings", "bridge", "overpass");
        addIfMentioned(special, lower, "rail line", "rail", "railway", "train");
        addIfMentioned(special, lower, "shoreline", "coast", "coastal", "beach", "harbor");
        addIfMentioned(special, lower, "district zoning", "district", "zoning");

        if (structures.isEmpty()) {
            structures.add("houses");
        }
        if ("abandoned".equals(plan.theme) || "apocalypse".equalsIgnoreCase(style)) {
            special.add("wrecked cars");
            special.add("barricades");
            special.add("overgrown vegetation");
        }
        if (containsAny(lower, "fog", "mist")) {
            special.add("low-visibility pockets");
        }

        plan.structures.addAll(structures);
        plan.specialFeatures.addAll(special);
        return plan;
    }

    private static String inferTheme(String lower, String style) {
        if (containsAny(lower, "abandoned", "apocalypse", "post-apocalyptic", "ruins", "walking dead")
                || "apocalypse".equalsIgnoreCase(style)) {
            return "abandoned";
        }
        if (containsAny(lower, "futuristic", "sci-fi", "cyber")) {
            return "futuristic";
        }
        if (containsAny(lower, "medieval", "castle", "village")) {
            return "historic";
        }
        return "realistic";
    }

    private static String inferTerrain(String lower) {
        if (containsAny(lower, "mountain", "cliff", "ridge", "alpine")) {
            return "steep mountains";
        }
        if (containsAny(lower, "hill", "rolling")) {
            return "rolling hills";
        }
        if (containsAny(lower, "swamp", "marsh", "wetland")) {
            return "wet lowlands";
        }
        if (containsAny(lower, "desert", "dune", "arid")) {
            return "arid flats";
        }
        return "mixed plains";
    }

    private static String inferRoads(String lower) {
        if (containsAny(lower, "grid", "downtown", "city core")) {
            return "urban grid";
        }
        if (containsAny(lower, "suburban", "suburb", "neighborhood")) {
            return "suburban curved";
        }
        if (containsAny(lower, "highway", "freeway", "interchange")) {
            return "arterial spine";
        }
        return "rural";
    }

    private static String inferVegetation(String lower) {
        if (containsAny(lower, "dense", "forest", "woods", "jungle")) {
            return "dense";
        }
        if (containsAny(lower, "sparse", "dry", "desert", "barren")) {
            return "sparse";
        }
        if (containsAny(lower, "park", "greenway", "garden")) {
            return "planned green spaces";
        }
        return "moderate";
    }

    private static boolean containsAny(String text, String... tokens) {
        for (String token : tokens) {
            if (text.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static void addIfMentioned(Set<String> values, String text, String value, String... tokens) {
        if (containsAny(text, tokens)) {
            values.add(value);
        }
    }
}
