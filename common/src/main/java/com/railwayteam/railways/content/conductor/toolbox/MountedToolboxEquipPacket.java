package com.railwayteam.railways.content.conductor.toolbox;

import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.multiloader.C2SPacket;
import com.zurrtum.create.content.equipment.toolbox.ToolboxInventory;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class MountedToolboxEquipPacket implements C2SPacket {
	public MountedToolboxEquipPacket(ConductorEntity toolboxCarrier, int slot, int hotbarSlot) {
	}

	public MountedToolboxEquipPacket(FriendlyByteBuf buffer) {
	}

	public void write(FriendlyByteBuf buffer) {
	}

	public void handle(ServerPlayer player) {
	}

	@ExpectPlatform
	public static void doEquip(ServerPlayer player, int hotbarSlot, ItemStack held, ToolboxInventory inv) {
	}
}
