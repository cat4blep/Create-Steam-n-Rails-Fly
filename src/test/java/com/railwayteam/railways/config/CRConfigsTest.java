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
        {"disableDatafixer":{"value":true},"registerMissingTracks":{"value":true}} | true
        {"disableDatafixer":true,"registerMissingTracks":true} | true
        {"disableDatafixer":{"value":false},"registerMissingTracks":{"value":false}} | false
        {"disableDatafixer":"true","registerMissingTracks":1} | false
        {"disableDatafixer":null,"registerMissingTracks":{}} | false
        {} | false
        invalid json | false
        null | false
        missing | false
        """)
    void readsEarlySettingsFromCommonConfig(String json, boolean expected) throws Exception {
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
            assertEquals(expected, CRConfigs.getDisableDatafixer());
            assertEquals(expected, CRConfigs.getRegisterMissingTracks());
        } finally {
            loaderDir.set(FabricLoader.getInstance(), previousDir);
            disable.set(null, previousDisable);
            tracks.set(null, previousTracks);
        }
    }
}
