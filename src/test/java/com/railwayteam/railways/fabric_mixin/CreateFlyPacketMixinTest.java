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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.MethodModel;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CreateFlyPacketMixinTest {
    @Test
    void clientShadowRestoreMatchesThePacketApiAndRunsAfterThreadDispatch() throws Exception {
        assertThreadSafePacketInjection(
            "com.zurrtum.create.client.AllHandle",
            "onAddTrain",
            "com.railwayteam.railways.fabric_mixin.client.MixinAllHandleShadowTrain",
            "railways$beginShadowRestore"
        );
    }

    @Test
    void serverShadowRestoreRunsAfterThreadDispatch() throws Exception {
        assertThreadSafePacketInjection(
            "com.zurrtum.create.AllHandle",
            "onTrainRelocation",
            "com.railwayteam.railways.fabric_mixin.MixinAllHandleTrainRelocationShadow",
            "railways$restoreShadowTrain"
        );
    }

    private static void assertThreadSafePacketInjection(
        String targetClass, String targetMethod, String mixinClass, String callbackName
    ) throws Exception {
        ClassLoader loader = CreateFlyPacketMixinTest.class.getClassLoader();
        Method callback = Arrays.stream(Class.forName(mixinClass, false, loader).getDeclaredMethods())
            .filter(method -> method.getName().equals(callbackName))
            .findFirst().orElseThrow();
        MethodModel target = readMethod(targetClass, targetMethod);
        String targetDescriptor = target.methodType().stringValue();
        String expectedCallback = targetDescriptor.replace(")V",
            "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V");
        assertEquals(expectedCallback, callbackDescriptor(callback));
        assertEquals(Modifier.isStatic(target.flags().flagsMask()), Modifier.isStatic(callback.getModifiers()));

        Inject injection = callback.getAnnotation(Inject.class);
        assertNotNull(injection);
        assertTrue(Arrays.asList(injection.method()).contains(targetMethod));
        assertTrue(injection.cancellable(), "Shadow train packets must bypass normal live-train handling");
        assertEquals(1, injection.at().length);
        At point = injection.at()[0];
        assertEquals("INVOKE", point.value(), "Packet state must not be touched before thread dispatch");
        assertEquals(At.Shift.AFTER, point.shift());
        assertTrue(point.target().contains("forceMainThread(")
            || point.target().contains("ensureRunningOnSameThread("));

        boolean invokesDispatch = target.code().orElseThrow().elementStream()
            .filter(InvokeInstruction.class::isInstance)
            .map(InvokeInstruction.class::cast)
            .anyMatch(invoke -> point.target().equals("L" + invoke.method().owner().asInternalName()
                + ";" + invoke.method().name().stringValue() + invoke.method().type().stringValue()));
        assertTrue(invokesDispatch, "The injection point must exist in the resolved Create Fly bytecode");
    }

    private static String callbackDescriptor(Method callback) {
        return "(" + Arrays.stream(callback.getParameterTypes())
            .map(Class::descriptorString).reduce("", String::concat) + ")" + callback.getReturnType().descriptorString();
    }

    private static MethodModel readMethod(String className, String methodName) throws IOException {
        InputStream resource = CreateFlyPacketMixinTest.class.getClassLoader()
            .getResourceAsStream(className.replace('.', '/') + ".class");
        assertNotNull(resource, () -> className + " is missing from the test runtime classpath");
        try (resource) {
            return ClassFile.of().parse(resource.readAllBytes()).methods().stream()
                .filter(method -> method.methodName().stringValue().equals(methodName))
                .findFirst().orElseThrow();
        }
    }
}
