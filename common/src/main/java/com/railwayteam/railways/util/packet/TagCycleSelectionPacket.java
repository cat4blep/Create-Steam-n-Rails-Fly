package com.railwayteam.railways.util.packet;

import com.railwayteam.railways.multiloader.C2SPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

public class TagCycleSelectionPacket implements C2SPacket {
    public TagCycleSelectionPacket(Item target) {
    }

    public TagCycleSelectionPacket(FriendlyByteBuf buf) {
    }

    public void write(FriendlyByteBuf buffer) {
    }

    public void handle(ServerPlayer sender) {
    }
}
