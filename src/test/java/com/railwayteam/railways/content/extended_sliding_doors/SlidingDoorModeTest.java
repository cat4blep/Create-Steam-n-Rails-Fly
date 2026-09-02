/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.extended_sliding_doors;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlidingDoorModeTest {
    @Test
    void translationKeysUseTheCreateNamespace() {
        for (SlidingDoorMode mode : SlidingDoorMode.values()) {
            assertEquals(
                "create.sliding_door.mode." + mode.name().toLowerCase(Locale.ROOT),
                mode.getTranslationKey()
            );
        }
    }
}
