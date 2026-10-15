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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.RecordBuilder;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StationLimitPersistenceTest {
    @Test
    void newStationsAndStationsWithoutAReservationRemainAvailable() {
        TestStation station = new TestStation();
        assertFalse(station.isLimitEnabled());
        assertTrue(station.isStationEnabled());
        assertNull(station.getDisablingTrain());

        station.setLimitEnabled(true);
        assertTrue(station.isStationEnabled());
        assertNull(station.orDisablingTrain(null, null));
    }

    @Test
    void saveDataRetainsBothFlagValuesAndOldSavesDefaultToDisabled() throws ReflectiveOperationException {
        TestStation station = new TestStation();
        Map<String, Boolean> saved = new HashMap<>();
        ValueOutput output = proxy(ValueOutput.class, (method, args) -> {
            assertEquals("putBoolean", method.getName());
            saved.put((String) args[0], (Boolean) args[1]);
            return null;
        });
        ValueInput input = proxy(ValueInput.class, (method, args) -> {
            assertEquals("getBooleanOr", method.getName());
            return saved.getOrDefault((String) args[0], (Boolean) args[1]);
        });

        for (boolean enabled : new boolean[] {true, false}) {
            station.setLimitEnabled(enabled);
            hook("railways$writeLimit", ValueOutput.class, DimensionPalette.class, CallbackInfo.class)
                .invoke(station, output, null, null);
            assertEquals(enabled, saved.get("LimitEnabled"));
            station.setLimitEnabled(!enabled);
            hook("railways$readLimit", ValueInput.class, boolean.class, DimensionPalette.class, CallbackInfo.class)
                .invoke(station, input, false, null, null);
            assertEquals(enabled, station.isLimitEnabled());
        }

        saved.clear();
        station.setLimitEnabled(true);
        hook("railways$readLimit", ValueInput.class, boolean.class, DimensionPalette.class, CallbackInfo.class)
            .invoke(station, input, true, null, null);
        assertFalse(station.isLimitEnabled());
    }

    @Test
    void dynamicSaveDataRoundTripsWithoutDroppingOtherStationFields() throws ReflectiveOperationException {
        TestStation station = new TestStation();
        Method encode = hook("railways$encodeLimit", DynamicOps.class, Operation.class);
        for (boolean enabled : new boolean[] {true, false}) {
            station.setLimitEnabled(enabled);
            Operation<RecordBuilder<JsonElement>> original = args -> JsonOps.INSTANCE.mapBuilder();
            @SuppressWarnings("unchecked")
            RecordBuilder<JsonElement> builder = (RecordBuilder<JsonElement>) encode.invoke(
                station, JsonOps.INSTANCE, original);
            builder.add("Name", new JsonPrimitive("Platform 2"));
            JsonElement saved = builder.build(JsonOps.INSTANCE.empty()).getOrThrow();
            assertEquals("Platform 2", saved.getAsJsonObject().get("Name").getAsString());
            assertEquals(enabled, saved.getAsJsonObject().get("LimitEnabled").getAsBoolean());

            station.setLimitEnabled(!enabled);
            decode(station, saved);
            assertEquals(enabled, station.isLimitEnabled());
        }
    }

    @Test
    void missingOrMalformedDynamicFlagDoesNotRetainAnOldTrueValue() throws ReflectiveOperationException {
        TestStation station = new TestStation();
        JsonObject malformed = new JsonObject();
        malformed.addProperty("LimitEnabled", "invalid");
        for (JsonElement saved : new JsonElement[] {new JsonObject(), malformed, new JsonPrimitive(42)}) {
            station.setLimitEnabled(true);
            decode(station, saved);
            assertFalse(station.isLimitEnabled());
        }
    }

    @Test
    void graphSyncRoundTripsTheFlagWithoutConsumingTheFollowingEdgePoint() throws ReflectiveOperationException {
        TestStation station = new TestStation();
        Method write = hook("railways$writeNetLimit", FriendlyByteBuf.class, DimensionPalette.class, CallbackInfo.class);
        Method read = hook("railways$readNetLimit", FriendlyByteBuf.class, DimensionPalette.class, CallbackInfo.class);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            for (boolean enabled : new boolean[] {true, false}) {
                buffer.clear();
                station.setLimitEnabled(enabled);
                write.invoke(station, buffer, null, null);
                buffer.writeVarInt(12345);
                station.setLimitEnabled(!enabled);
                read.invoke(station, buffer, null, null);
                assertEquals(enabled, station.isLimitEnabled());
                assertEquals(12345, buffer.readVarInt());
                assertFalse(buffer.isReadable());
            }
        } finally {
            buffer.release();
        }
    }

    private static void decode(TestStation station, JsonElement saved) throws ReflectiveOperationException {
        hook("railways$decodeLimit", DynamicOps.class, Object.class, boolean.class,
            DimensionPalette.class, CallbackInfo.class).invoke(station, JsonOps.INSTANCE, saved, false, null, null);
    }

    private static Method hook(String name, Class<?>... parameters) throws NoSuchMethodException {
        Method method = MixinGlobalStationLimit.class.getDeclaredMethod(name, parameters);
        assertTrue(method.trySetAccessible());
        return method;
    }

    private static <T> T proxy(Class<T> type, ProxyCall call) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type},
            (instance, method, args) -> call.invoke(method, args)));
    }

    @FunctionalInterface
    private interface ProxyCall {
        Object invoke(Method method, Object[] args);
    }

    private static final class TestStation extends MixinGlobalStationLimit {
        @Override
        public Train getNearestTrain() {
            return null;
        }
    }
}
