/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.base.datafixerapi;

import net.minecraft.util.datafix.DataFixTypes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataFixesInternalsImplTest {
    @Test
    void bypassesOnlyUnversionedMapRoots() {
        assertTrue(DataFixesInternalsImpl.shouldBypassModFixers(DataFixTypes.ADVANCEMENTS));
        assertTrue(DataFixesInternalsImpl.shouldBypassModFixers(DataFixTypes.STATS));
        assertFalse(DataFixesInternalsImpl.shouldBypassModFixers(DataFixTypes.PLAYER));
    }
}
