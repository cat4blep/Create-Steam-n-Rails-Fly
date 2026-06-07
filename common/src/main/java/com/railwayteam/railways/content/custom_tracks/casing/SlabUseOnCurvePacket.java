package com.railwayteam.railways.content.custom_tracks.casing;

import com.railwayteam.railways.multiloader.C2SPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class SlabUseOnCurvePacket implements C2SPacket {
    private final BlockPos pos;
    private final BlockPos targetPos;
    private final BlockPos soundSource;

    public SlabUseOnCurvePacket(BlockPos pos, BlockPos targetPos, BlockPos soundSource) {
        this.pos = pos;
        this.targetPos = targetPos;
        this.soundSource = soundSource;
    }

    public SlabUseOnCurvePacket(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        targetPos = buffer.readBlockPos();
        soundSource = buffer.readBlockPos();
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeBlockPos(targetPos);
        buffer.writeBlockPos(soundSource);
    }

    public void handle(ServerPlayer player) {
    }
}
