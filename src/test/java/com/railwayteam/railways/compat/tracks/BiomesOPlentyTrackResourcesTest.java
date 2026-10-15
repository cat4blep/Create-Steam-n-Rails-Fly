package com.railwayteam.railways.compat.tracks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BiomesOPlentyTrackResourcesTest {
    @Test
    void newTrackFamiliesHaveCompleteModelsRecipesAndConditionalLoot() throws Exception {
        Set<String> visitedModels = new HashSet<>();
        JsonObject tracks = readJson("data/create/tags/block/tracks.json");
        JsonObject english = readJson("assets/railways/lang/en_us.json");
        for (String wood : List.of("empyreal", "maple", "pine")) {
            for (String gauge : List.of("", "_narrow", "_wide")) {
                String id = "track_biomesoplenty_" + wood + gauge;
                String incomplete = "track_incomplete_biomesoplenty_" + wood + gauge;
                checkModelReferences(readJson("assets/railways/blockstates/" + id + ".json"), visitedModels);
                checkModelReferences(readJson("assets/railways/items/" + id + ".json"), visitedModels);
                checkModelReferences(readJson("assets/railways/items/" + incomplete + ".json"), visitedModels);
                // Curve segments are registered independently of blockstate models.
                for (String part : List.of("segment_left", "segment_right", "tie")) {
                    checkModel("railways:block/track/compat/biomesoplenty/" + wood + gauge + "/" + part, visitedModels);
                }

                JsonObject recipe = readJson("data/railways/recipe/sequenced_assembly/" + id + ".json");
                assertEquals("railways:" + id, recipe.getAsJsonObject("result").get("id").getAsString());
                assertEquals("railways:" + incomplete, recipe.getAsJsonObject("transitional_item").get("id").getAsString());
                assertModCondition(recipe);
                assertModCondition(readJson("data/railways/loot_table/blocks/" + id + ".json"));
                assertTrue(tracks.getAsJsonArray("values").asList().stream().anyMatch(value ->
                    value.isJsonObject() && value.getAsJsonObject().get("id").getAsString().equals("railways:" + id)), id);
                assertTrue(english.has("block.railways." + id));
                assertTrue(english.has("item.railways." + incomplete));
            }
        }
    }

    private void assertModCondition(JsonObject resource) {
        JsonElement conditions = resource.get("fabric:load_conditions");
        assertNotNull(conditions, "Optional BOP resources must have a load condition");
        List<JsonElement> entries = conditions.isJsonArray() ? conditions.getAsJsonArray().asList() : List.of(conditions);
        assertTrue(entries.stream().anyMatch(condition -> {
            JsonObject value = condition.getAsJsonObject();
            return value.get("condition").getAsString().equals("fabric:all_mods_loaded")
                && value.getAsJsonArray("values").asList().stream().anyMatch(mod -> mod.getAsString().equals("biomesoplenty"));
        }), "Optional BOP resources must not load without Biomes O' Plenty");
    }

    private void checkModelReferences(JsonElement element, Set<String> visited) throws Exception {
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) checkModelReferences(child, visited);
        } else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            for (String key : List.of("parent", "model")) {
                if (object.has(key) && object.get(key).isJsonPrimitive()) checkModel(object.get(key).getAsString(), visited);
            }
            if (object.has("textures")) {
                for (JsonElement texture : object.getAsJsonObject("textures").asMap().values()) {
                    String name = texture.getAsString();
                    if (name.startsWith("railways:") || name.startsWith("biomesoplenty:")) {
                        String[] parts = name.split(":", 2);
                        assertNotNull(getClass().getClassLoader().getResource("assets/" + parts[0] + "/textures/" + parts[1] + ".png"), name);
                    }
                }
            }
            for (JsonElement child : object.asMap().values()) {
                if (child.isJsonObject() || child.isJsonArray()) checkModelReferences(child, visited);
            }
        }
    }

    private void checkModel(String name, Set<String> visited) throws Exception {
        if (!name.startsWith("railways:") || !visited.add(name)) return;
        if (name.endsWith(".obj")) {
            assertNotNull(getClass().getClassLoader().getResource("assets/railways/" + name.substring("railways:".length())), name);
            return;
        }
        checkModelReferences(readJson("assets/railways/models/" + name.substring("railways:".length()) + ".json"), visited);
    }

    private JsonObject readJson(String resource) throws Exception {
        try (var input = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(input, resource);
            try (var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        }
    }
}
