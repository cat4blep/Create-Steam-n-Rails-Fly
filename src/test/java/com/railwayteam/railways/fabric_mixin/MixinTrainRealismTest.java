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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MixinTrainRealismTest {
    @Test
    void treatsForwardAndReverseMagnitudeSymmetrically() {
        assertTrue(MixinTrainRealism.railways$shouldLimitAcceleration(0.5, 0.25));
        assertTrue(MixinTrainRealism.railways$shouldLimitAcceleration(-0.5, -0.25));

        assertFalse(MixinTrainRealism.railways$shouldLimitAcceleration(0.2, 0.4));
        assertFalse(MixinTrainRealism.railways$shouldLimitAcceleration(-0.2, -0.4));
        assertFalse(MixinTrainRealism.railways$shouldLimitAcceleration(0.0, 0.4));
        assertFalse(MixinTrainRealism.railways$shouldLimitAcceleration(0.0, -0.4));
    }
}
