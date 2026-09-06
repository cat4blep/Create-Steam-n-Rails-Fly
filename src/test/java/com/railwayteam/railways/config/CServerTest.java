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

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.zurrtum.create.catnip.config.Builder;
import net.fabricmc.loader.api.FabricLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CServerTest {
    @TempDir Path configDir;
    private Field loaderConfigDir;
    private Object previousConfigDir;
    private CServer config;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        // Builder uses Fabric's config directory even outside the game.
        loaderConfigDir = FabricLoader.getInstance().getClass().getDeclaredField("configDir");
        loaderConfigDir.setAccessible(true);
        previousConfigDir = loaderConfigDir.get(FabricLoader.getInstance());
        loaderConfigDir.set(FabricLoader.getInstance(), configDir);
        config = Builder.create(CServer::new, "railways", "server");
    }

    @AfterEach
    void restoreLoaderDirectory() throws IllegalAccessException {
        loaderConfigDir.set(FabricLoader.getInstance(), previousConfigDir);
    }

    @Test
    void appliesServerValuesAndRestoresLocalValuesAcrossConnections() {
        JsonObject local = config.getValues();
        JsonObject original = local.deepCopy();
        JsonObject remote = local.deepCopy();
        remote.getAsJsonObject("misc").getAsJsonObject("strictCoupler").addProperty("value", true);
        remote.getAsJsonObject("semaphores").getAsJsonObject("flipYellowOrder").addProperty("value", true);

        config.reload(remote);
        assertTrue(config.strictCoupler.get());
        assertTrue(config.semaphores.flipYellowOrder.get());
        assertSame(local, config.getValues(), "saving config must keep the local root");
        assertEquals(original, local);

        config.reload(remote.deepCopy());
        config.reload(null);
        assertFalse(config.strictCoupler.get());
        assertFalse(config.semaphores.flipYellowOrder.get());
        assertSame(local, config.getValues());
        assertEquals(original, local);

        config.reload(remote.deepCopy());
        assertTrue(config.strictCoupler.get());
        config.reload(null);
        assertFalse(config.strictCoupler.get());
    }

    @Test
    void malformedNestedGroupRollsBackWithoutCorruptingTheLocalRoot() {
        JsonObject local = config.getValues();
        JsonObject original = local.deepCopy();
        JsonObject malformed = local.deepCopy();
        malformed.getAsJsonObject("misc").getAsJsonObject("strictCoupler").addProperty("value", true);
        malformed.addProperty("semaphores", "invalid group");
        assertThrows(RuntimeException.class, () -> config.reload(malformed));
        assertFalse(config.strictCoupler.get());
        assertFalse(config.semaphores.flipYellowOrder.get());
        assertSame(local, config.getValues());
        assertEquals(original, local);
        config.reload(original.deepCopy());
        config.reload(null);
        assertEquals(original, config.getValues());
    }

    @Test
    void rejectsNullConfigValueBeforeItCanReachGameplay() {
        JsonObject malformed = config.getValues().deepCopy();
        malformed.getAsJsonObject("misc").getAsJsonObject("strictCoupler").add("value", JsonNull.INSTANCE);
        assertThrows(RuntimeException.class, () -> config.reload(malformed));
        assertFalse(config.strictCoupler.get());
    }
}
