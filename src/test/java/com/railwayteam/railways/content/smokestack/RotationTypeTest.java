/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.smokestack;

import net.minecraft.SharedConstants;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RotationTypeTest {
    @Test
    void rotatesHorizontalOnlyAxesWithoutUsingThePillarProperty() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        for (Axis axis : new Axis[] {Axis.X, Axis.Z}) {
            // Nether portals have the same horizontal-only property as axial
            // smokestacks, unlike RotatedPillarBlock's three-axis property.
            BlockState state = Blocks.NETHER_PORTAL.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_AXIS, axis);
            for (Rotation rotation : Rotation.values()) {
                Axis expected = rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90
                    ? (axis == Axis.X ? Axis.Z : Axis.X) : axis;
                assertEquals(expected, RotationType.AXIS.rotate(state, rotation).getValue(BlockStateProperties.HORIZONTAL_AXIS));
            }
        }
    }
}
