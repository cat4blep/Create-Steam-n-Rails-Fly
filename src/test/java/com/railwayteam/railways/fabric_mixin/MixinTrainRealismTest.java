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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MixinTrainRealismTest {
    @Test
    void helperRemainsMixinSafeAndTreatsForwardAndReverseMagnitudeSymmetrically()
        throws ReflectiveOperationException {
        Method helper = MixinTrainRealism.class.getDeclaredMethod(
            "railways$shouldLimitAcceleration", double.class, double.class
        );
        assertTrue(Modifier.isPrivate(helper.getModifiers()));
        assertTrue(Modifier.isStatic(helper.getModifiers()));
        assertTrue(helper.trySetAccessible());

        assertTrue(shouldLimitAcceleration(helper, 0.5, 0.25));
        assertTrue(shouldLimitAcceleration(helper, -0.5, -0.25));

        assertFalse(shouldLimitAcceleration(helper, 0.2, 0.4));
        assertFalse(shouldLimitAcceleration(helper, -0.2, -0.4));
        assertFalse(shouldLimitAcceleration(helper, 0.0, 0.4));
        assertFalse(shouldLimitAcceleration(helper, 0.0, -0.4));
    }

    private static boolean shouldLimitAcceleration(Method helper, double target, double currentSpeed)
        throws ReflectiveOperationException {
        return (boolean) helper.invoke(null, target, currentSpeed);
    }
}
