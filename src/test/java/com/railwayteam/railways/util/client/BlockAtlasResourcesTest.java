/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.util.client;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.renderer.texture.atlas.sources.SourceFilter;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BlockAtlasResourcesTest {
    @Test
    void excludesOnlyLegacySheetsAndKeepsTheirGeneratedReplacements() throws Exception {
        var loader = getClass().getClassLoader();
        SourceFilter filter;
        try (var stream = loader.getResourceAsStream("assets/minecraft/atlases/blocks.json")) {
            assertNotNull(stream);
            var sources = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                .getAsJsonObject().getAsJsonArray("sources");
            var definition = sources.asList().stream()
                .filter(source -> source.getAsJsonObject().get("type").getAsString().equals("minecraft:filter"))
                .findFirst().orElseThrow();
            filter = SourceFilter.MAP_CODEC.codec().parse(JsonOps.INSTANCE, definition).getOrThrow();
        }
        var matches = filter.filter().locationPredicate();
        assertFalse(matches.test(Identifier.parse("create:block/fuel_tank_connected")));
        assertFalse(matches.test(Identifier.parse("railways:block/fuel_tank_connected/1")));
        assertFalse(matches.test(Identifier.parse("railways:block/fuel_tank")));

        Path textures = Path.of(loader.getResource("assets/railways/textures/block").toURI());
        int excluded = 0;
        try (var paths = Files.walk(textures)) {
            for (Path png : paths.filter(path -> path.toString().endsWith(".png")).toList()) {
                String relative = textures.relativize(png).toString().replace('\\', '/');
                Identifier sprite = Identifier.fromNamespaceAndPath("railways", "block/" + relative.substring(0, relative.length() - 4));
                if (!matches.test(sprite)) continue;
                excluded++;
                Path tiles = png.resolveSibling(png.getFileName().toString().replace(".png", ""));
                assertTrue(Files.isDirectory(tiles), () -> "Excluded sheet has no CT tiles: " + png);
                try (var variants = Files.list(tiles)) {
                    var generated = variants.filter(path -> path.toString().endsWith(".png")).toList();
                    assertFalse(generated.isEmpty(), () -> "Excluded sheet has no CT variants: " + png);
                    for (Path tile : generated) {
                        String name = tile.getFileName().toString().replace(".png", "");
                        assertFalse(matches.test(sprite.withSuffix("/" + name)), () -> "Filter removes CT variant: " + tile);
                    }
                }
            }
        }
        assertTrue(excluded > 0, "The filter must remove actual redundant sheets from the built resources");
    }
}
