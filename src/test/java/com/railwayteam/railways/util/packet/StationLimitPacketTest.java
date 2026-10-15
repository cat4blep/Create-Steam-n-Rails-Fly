/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.util.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StationLimitPacketTest {
    @Test
    void packetRoundTripsPositionAndFlagWithoutConsumingTheNextPayload() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        FriendlyByteBuf rewritten = new FriendlyByteBuf(Unpooled.buffer());
        BlockPos pos = new BlockPos(-213, 104, 4096);
        try {
            for (boolean enabled : new boolean[] {true, false}) {
                buffer.clear();
                rewritten.clear();
                new StationLimitPacket(pos, enabled).write(buffer);
                buffer.writeInt(0x13572468);
                new StationLimitPacket(buffer).write(rewritten);
                assertEquals(pos, rewritten.readBlockPos());
                assertEquals(enabled, rewritten.readBoolean());
                assertFalse(rewritten.isReadable());
                assertEquals(0x13572468, buffer.readInt());
                assertFalse(buffer.isReadable());
            }
        } finally {
            buffer.release();
            rewritten.release();
        }
    }

    @Test
    void packetKeepsItsOriginalStationWhenTheCallerReusesAMutablePosition() {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(12, 64, 34);
        StationLimitPacket packet = new StationLimitPacket(mutable, true);
        mutable.set(48, 80, 96);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            packet.write(buffer);
            assertEquals(new BlockPos(12, 64, 34), buffer.readBlockPos());
            assertTrue(buffer.readBoolean());
        } finally {
            buffer.release();
        }
    }
}
