/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricMixinRegistrationTest {
    @Test
    void portedRuntimeFixesRemainRegistered() throws IOException {
        Set<String> registered = loadRegisteredMixins();
        for (String mixin : List.of(
            "MixinAbstractContainerMenu",
            "MixinCarriageBufferDistance",
            "MixinContainerLevelAccess",
            "MixinItemCombinerMenu",
            "MixinNavigationBuffer",
            "MixinTrainBufferBlocked"
        )) {
            assertTrue(registered.contains(mixin), () -> mixin + " is not registered in railways.mixins.json");
        }
    }

    private static Set<String> loadRegisteredMixins() throws IOException {
        InputStream resource = FabricMixinRegistrationTest.class.getClassLoader()
            .getResourceAsStream("railways.mixins.json");
        assertNotNull(resource, "railways.mixins.json is missing from the test runtime classpath");

        try (resource; InputStreamReader reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
            JsonArray mixins = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("mixins");
            Set<String> registered = new HashSet<>();
            mixins.forEach(element -> registered.add(element.getAsString()));
            return registered;
        }
    }
}
