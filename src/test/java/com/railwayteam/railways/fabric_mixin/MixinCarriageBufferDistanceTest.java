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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.classfile.ClassFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MixinCarriageBufferDistanceTest {
    @Test
    void persistenceHandlersMatchTheCurrentCarriageSerializationApi() throws IOException {
        String carriage = "com/zurrtum/create/content/trains/entity/Carriage.class";
        String mixin = "com/railwayteam/railways/fabric_mixin/MixinCarriageBufferDistance.class";

        assertHasMethod(
            carriage,
            "write",
            "(Lnet/minecraft/world/level/storage/ValueOutput;"
                + "Lcom/zurrtum/create/content/trains/graph/DimensionPalette;)V"
        );
        assertHasMethod(
            carriage,
            "read",
            "(Lnet/minecraft/world/level/storage/ValueInput;"
                + "Lcom/zurrtum/create/content/trains/graph/TrackGraph;"
                + "Lcom/zurrtum/create/content/trains/graph/DimensionPalette;)"
                + "Lcom/zurrtum/create/content/trains/entity/Carriage;"
        );
        assertHasMethod(
            carriage,
            "encode",
            "(Lcom/zurrtum/create/content/trains/entity/Carriage;"
                + "Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;"
                + "Lcom/zurrtum/create/content/trains/graph/DimensionPalette;)"
                + "Lcom/mojang/serialization/DataResult;"
        );
        assertHasMethod(
            carriage,
            "decode",
            "(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;"
                + "Lcom/zurrtum/create/content/trains/graph/TrackGraph;"
                + "Lcom/zurrtum/create/content/trains/graph/DimensionPalette;)"
                + "Lcom/zurrtum/create/content/trains/entity/Carriage;"
        );

        assertHasMethod(
            mixin,
            "railways$writeBufferDistances",
            "(Lnet/minecraft/world/level/storage/ValueOutput;"
                + "Lcom/zurrtum/create/content/trains/graph/DimensionPalette;"
                + "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V"
        );
        assertHasMethod(
            mixin,
            "railways$readBufferDistances",
            "(Lnet/minecraft/world/level/storage/ValueInput;"
                + "Lcom/zurrtum/create/content/trains/graph/TrackGraph;"
                + "Lcom/zurrtum/create/content/trains/graph/DimensionPalette;"
                + "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        );
        assertHasMethod(
            mixin,
            "railways$encodeBufferDistances",
            "(Lcom/mojang/serialization/DynamicOps;"
                + "Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;"
                + "Lcom/zurrtum/create/content/trains/entity/Carriage;)"
                + "Lcom/mojang/serialization/RecordBuilder;"
        );
        assertHasMethod(
            mixin,
            "railways$decodeBufferDistances",
            "(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;"
                + "Lcom/zurrtum/create/content/trains/graph/TrackGraph;"
                + "Lcom/zurrtum/create/content/trains/graph/DimensionPalette;"
                + "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V"
        );
    }

    @Test
    void bufferDistancesStartUnknownAndRejectNegativeValues() {
        TestMixin mixin = new TestMixin();
        assertNull(mixin.railways$getLeadingDistance());
        assertNull(mixin.railways$getTrailingDistance());

        mixin.railways$setLeadingDistance(-4);
        mixin.railways$setTrailingDistance(12);
        assertEquals(0, mixin.railways$getLeadingDistance());
        assertEquals(12, mixin.railways$getTrailingDistance());
    }

    private static final class TestMixin extends MixinCarriageBufferDistance {
    }

    private static void assertHasMethod(String classResource, String methodName, String descriptor)
        throws IOException {
        InputStream resource = MixinCarriageBufferDistanceTest.class.getClassLoader()
            .getResourceAsStream(classResource);
        assertNotNull(resource, () -> classResource + " is missing from the test runtime classpath");

        byte[] classBytes;
        try (resource) {
            classBytes = resource.readAllBytes();
        }
        boolean present = ClassFile.of().parse(classBytes).methods().stream()
            .anyMatch(method -> method.methodName().stringValue().equals(methodName)
                && method.methodType().stringValue().equals(descriptor));
        assertTrue(present, () -> methodName + descriptor + " is missing from " + classResource);
    }
}
