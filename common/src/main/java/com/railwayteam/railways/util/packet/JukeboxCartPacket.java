package com.railwayteam.railways.util.packet;

import com.railwayteam.railways.multiloader.S2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class JukeboxCartPacket implements S2CPacket {
    public JukeboxCartPacket(Entity target, ItemStack disc) {
    }

    public JukeboxCartPacket(FriendlyByteBuf buf) {
    }

    public void write(FriendlyByteBuf buffer) {
    }

    public void handle(Minecraft mc) {
    }
}
