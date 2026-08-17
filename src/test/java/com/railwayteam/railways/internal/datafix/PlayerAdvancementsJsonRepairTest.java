/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.internal.datafix;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static com.railwayteam.railways.internal.datafix.PlayerAdvancementsJsonRepair.LEGACY_DATA_VERSION_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerAdvancementsJsonRepairTest {
    @Test
    void repairsExactDamagedRootWithoutMutatingParsedInput() {
        JsonObject damaged = JsonParser.parseString("""
            {
              "Railways_DataVersion": 11,
              "minecraft:story/root": {
                "criteria": {"crafting_table": "2026-08-17 12:00:00 +0000"},
                "done": true
              }
            }
            """).getAsJsonObject();

        PlayerAdvancementsJsonRepair.RepairResult result = PlayerAdvancementsJsonRepair.repair(damaged);

        assertTrue(result.repaired());
        assertFalse(result.value().getAsJsonObject().has(LEGACY_DATA_VERSION_KEY));
        assertTrue(result.value().getAsJsonObject().has("minecraft:story/root"));
        assertEquals(11, damaged.get(LEGACY_DATA_VERSION_KEY).getAsInt());
    }

    @Test
    void leavesRootWithoutLegacyKeyUnchanged() {
        JsonObject valid = JsonParser.parseString("""
            {"minecraft:story/root": {"criteria": {}, "done": false}}
            """).getAsJsonObject();

        PlayerAdvancementsJsonRepair.RepairResult result = PlayerAdvancementsJsonRepair.repair(valid);

        assertFalse(result.repaired());
        assertSame(valid, result.value());
    }

    @Test
    void leavesScalarAndArrayRootsUnchanged() {
        JsonElement scalar = JsonParser.parseString("11");
        JsonArray array = JsonParser.parseString("[11, {\"Railways_DataVersion\": 12}]").getAsJsonArray();

        PlayerAdvancementsJsonRepair.RepairResult scalarResult = PlayerAdvancementsJsonRepair.repair(scalar);
        PlayerAdvancementsJsonRepair.RepairResult arrayResult = PlayerAdvancementsJsonRepair.repair(array);

        assertFalse(scalarResult.repaired());
        assertSame(scalar, scalarResult.value());
        assertFalse(arrayResult.repaired());
        assertSame(array, arrayResult.value());
    }

    @Test
    void preservesNestedLegacyKey() {
        JsonObject damaged = JsonParser.parseString("""
            {
              "Railways_DataVersion": 11,
              "railways:root": {
                "criteria": {"Railways_DataVersion": 7},
                "done": true
              }
            }
            """).getAsJsonObject();

        JsonObject repaired = PlayerAdvancementsJsonRepair.repair(damaged).value().getAsJsonObject();

        assertFalse(repaired.has(LEGACY_DATA_VERSION_KEY));
        assertEquals(7, repaired.getAsJsonObject("railways:root")
            .getAsJsonObject("criteria")
            .get(LEGACY_DATA_VERSION_KEY)
            .getAsInt());
    }

    @Test
    void preservesVanillaDataVersionAndAdvancementObjects() {
        JsonObject damaged = JsonParser.parseString("""
            {
              "DataVersion": 4440,
              "Railways_DataVersion": 11,
              "minecraft:adventure/root": {
                "criteria": {"killed_something": "2026-08-17 12:00:00 +0000"},
                "done": true
              }
            }
            """).getAsJsonObject();
        JsonElement advancement = damaged.get("minecraft:adventure/root").deepCopy();

        JsonObject repaired = PlayerAdvancementsJsonRepair.repair(damaged).value().getAsJsonObject();

        assertEquals(4440, repaired.get("DataVersion").getAsInt());
        assertEquals(advancement, repaired.get("minecraft:adventure/root"));
        assertFalse(repaired.has(LEGACY_DATA_VERSION_KEY));
    }
}
