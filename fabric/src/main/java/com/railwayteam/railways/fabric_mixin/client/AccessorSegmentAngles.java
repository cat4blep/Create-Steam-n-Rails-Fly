/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin.client;

import com.zurrtum.create.client.content.trains.track.TrackRenderer.SegmentAngles;
import com.zurrtum.create.content.trains.track.BezierConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Constructor factory for Create Fly's package-private SegmentAngles type. */
@Mixin(value = SegmentAngles.class, remap = false)
public interface AccessorSegmentAngles {
    @Invoker("<init>")
    static SegmentAngles railways$create(BezierConnection connection) {
        throw new AssertionError();
    }
}
