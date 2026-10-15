/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.handcar;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandcarSoundResourcesTest {
    @Test
    void handcarCogLoopUsesAMonoFileSoMinecraftCanAttenuateIt() throws Exception {
        var loader = getClass().getClassLoader();
        try (var stream = loader.getResourceAsStream("assets/railways/sounds.json")) {
            assertNotNull(stream);
            var sounds = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                .getAsJsonObject().getAsJsonObject("handcar_cogs").getAsJsonArray("sounds");
            assertEquals(1, sounds.size());
            var sound = sounds.get(0).getAsJsonObject();
            assertEquals("railways:handcar_cogs", sound.get("name").getAsString());
            assertTrue(sound.get("attenuation_distance").getAsInt() > 0);
        }

        try (var stream = loader.getResourceAsStream("assets/railways/sounds/handcar_cogs.ogg")) {
            assertNotNull(stream);
            byte[] ogg = stream.readAllBytes();
            assertEquals("OggS", new String(ogg, 0, 4, StandardCharsets.US_ASCII));
            byte[] identificationPacket = {1, 'v', 'o', 'r', 'b', 'i', 's'};
            int packet = -1;
            for (int i = 0; i <= ogg.length - 30; i++) {
                if (Arrays.equals(ogg, i, i + identificationPacket.length,
                    identificationPacket, 0, identificationPacket.length)) {
                    packet = i;
                    break;
                }
            }
            assertTrue(packet >= 0, "Missing Vorbis identification packet");
            assertEquals(1, Byte.toUnsignedInt(ogg[packet + 11]),
                "Stereo cog audio plays without positional distance attenuation");
        }
    }
}
