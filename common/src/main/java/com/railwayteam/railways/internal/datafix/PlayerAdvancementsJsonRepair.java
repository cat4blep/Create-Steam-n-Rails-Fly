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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class PlayerAdvancementsJsonRepair {
    public static final String LEGACY_DATA_VERSION_KEY = "Railways_DataVersion";

    private PlayerAdvancementsJsonRepair() {
    }

    public static RepairResult repair(JsonElement parsed) {
        if (!(parsed instanceof JsonObject root) || !root.has(LEGACY_DATA_VERSION_KEY))
            return new RepairResult(parsed, false);

        JsonObject repaired = root.deepCopy();
        repaired.remove(LEGACY_DATA_VERSION_KEY);
        return new RepairResult(repaired, true);
    }

    public record RepairResult(JsonElement value, boolean repaired) {
    }
}
