package com.railwayteam.railways.content.conductor.toolbox;

import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.multiloader.C2SPacket;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class MountedToolboxDisposeAllPacket implements C2SPacket {
	public MountedToolboxDisposeAllPacket(ConductorEntity toolboxCarrier) {
	}

	public MountedToolboxDisposeAllPacket(FriendlyByteBuf buffer) {
	}

	public void write(FriendlyByteBuf buffer) {
	}

	public void handle(ServerPlayer player) {
	}

	@ExpectPlatform
	public static boolean doDisposal(MountedToolbox toolbox, ServerPlayer player, ConductorEntity conductor) {
		return false;
	}
}
