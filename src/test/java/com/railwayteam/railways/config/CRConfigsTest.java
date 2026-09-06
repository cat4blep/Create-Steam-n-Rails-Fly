/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.config;

import com.google.gson.JsonParser;
import com.zurrtum.create.catnip.config.Builder;
import net.fabricmc.loader.api.FabricLoader;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CRConfigsTest {
    @TempDir Path configDir;

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        {"disableDatafixer":{"value":true},"registerMissingTracks":{"value":true}} | true | true
        {"disableDatafixer":true,"registerMissingTracks":true} | true | true
        {"disableDatafixer":{"value":false},"registerMissingTracks":{"value":false}} | false | false
        {"disableDatafixer":"true","registerMissingTracks":1} | false | true
        {"disableDatafixer":null,"registerMissingTracks":{}} | false | true
        {} | false | true
        invalid json | false | true
        null | false | true
        missing | false | true
        """)
    void readsEarlySettingsFromCommonConfig(String json, boolean expectedDisableDatafixer, boolean expectedTracks) throws Exception {
        Field loaderDir = FabricLoader.getInstance().getClass().getDeclaredField("configDir");
        Field disable = CRConfigs.class.getDeclaredField("cachedDisableDatafixer");
        Field tracks = CRConfigs.class.getDeclaredField("cachedRegisterMissingTracks");
        loaderDir.setAccessible(true);
        disable.setAccessible(true);
        tracks.setAccessible(true);
        Object previousDir = loaderDir.get(FabricLoader.getInstance());
        Object previousDisable = disable.get(null);
        Object previousTracks = tracks.get(null);
        try {
            Files.createDirectories(configDir.resolve("railways"));
            if (!json.equals("missing"))
                Files.writeString(configDir.resolve("railways/common.json"), json);
            loaderDir.set(FabricLoader.getInstance(), configDir);
            disable.set(null, null);
            tracks.set(null, null);
            assertEquals(expectedDisableDatafixer, CRConfigs.getDisableDatafixer());
            assertEquals(expectedTracks, CRConfigs.getRegisterMissingTracks());
        } finally {
            loaderDir.set(FabricLoader.getInstance(), previousDir);
            disable.set(null, previousDisable);
            tracks.set(null, previousTracks);
        }
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        missing | true
        {"registerMissingTracks":{"value":false}} | false
        """)
    void generatedConfigAndNextStartupAgreeWithInitialRegistration(String json, boolean expected) throws Exception {
        Field loaderDir = FabricLoader.getInstance().getClass().getDeclaredField("configDir");
        Field tracks = CRConfigs.class.getDeclaredField("cachedRegisterMissingTracks");
        Field disable = CRConfigs.class.getDeclaredField("cachedDisableDatafixer");
        loaderDir.setAccessible(true);
        tracks.setAccessible(true);
        disable.setAccessible(true);
        Object previousDir = loaderDir.get(FabricLoader.getInstance());
        Object previousTracks = tracks.get(null);
        Object previousDisable = disable.get(null);
        try {
            Path file = configDir.resolve("railways/common.json");
            if (!json.equals("missing")) {
                Files.createDirectories(file.getParent());
                Files.writeString(file, json);
            }
            loaderDir.set(FabricLoader.getInstance(), configDir);
            tracks.set(null, null);
            disable.set(null, null);
            assertEquals(expected, CRConfigs.getRegisterMissingTracks(), "block registration before config creation");

            CCommon config = Builder.create(CCommon::new, "railways", "common");
            assertEquals(expected, config.registerMissingTracks.get());
            assertEquals(false, config.disableDatafixer.get());
            var saved = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            assertEquals(expected, saved.getAsJsonObject("registerMissingTracks").get("value").getAsBoolean());

            tracks.set(null, null);
            disable.set(null, null);
            assertEquals(expected, CRConfigs.getRegisterMissingTracks(), "block registration on the next startup");
        } finally {
            loaderDir.set(FabricLoader.getInstance(), previousDir);
            tracks.set(null, previousTracks);
            disable.set(null, previousDisable);
        }
    }
}
