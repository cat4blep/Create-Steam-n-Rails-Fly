package com.railwayteam.railways.content.conductor.toolbox;

import com.railwayteam.railways.registry.CRContainerTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class MountedToolboxContainer extends AbstractContainerMenu {
	public MountedToolboxContainer(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
		super(type, id);
	}

	public MountedToolboxContainer(MenuType<?> type, int id, Inventory inv, MountedToolbox toolbox) {
		super(type, id);
	}

	public static MountedToolboxContainer create(int id, Inventory inv, MountedToolbox toolbox) {
		return new MountedToolboxContainer(CRContainerTypes.MOUNTED_TOOLBOX.get(), id, inv, toolbox);
	}

	public boolean stillValid(Player player) {
		return true;
	}

	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}
}
