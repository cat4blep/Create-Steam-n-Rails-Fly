package com.railwayteam.railways.content.conductor.toolbox;

import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;

public class MountedToolbox extends ToolboxBlockEntity {
    private final ConductorEntity parent;
    private final DyeColor color;

    public MountedToolbox(ConductorEntity parent, DyeColor color) {
        super(BlockPos.ZERO, Blocks.BROWN_WOOL.defaultBlockState());
        this.parent = parent;
        this.color = color;
    }

    public ConductorEntity getParent() {
        return parent;
    }

    public DyeColor getColor() {
        return color;
    }

    public void read(CompoundTag tag, boolean clientPacket) {
    }

    public CompoundTag write(boolean clientPacket) {
        return new CompoundTag();
    }

    public static MountedToolbox read(ConductorEntity parent, CompoundTag compound) {
        return new MountedToolbox(parent, DyeColor.BROWN);
    }

    public static void openMenu(ServerPlayer player, MountedToolbox toolbox) {
    }
}
