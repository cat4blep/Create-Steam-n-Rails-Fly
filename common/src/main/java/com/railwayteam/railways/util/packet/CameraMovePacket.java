package com.railwayteam.railways.util.packet;

import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.multiloader.C2SPacket;
import com.railwayteam.railways.multiloader.S2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;

public class CameraMovePacket implements C2SPacket, S2CPacket {
    public CameraMovePacket(ConductorEntity entity, ServerboundMovePlayerPacket.PosRot packet) {
    }

    public CameraMovePacket(FriendlyByteBuf buf) {
    }

    public void write(FriendlyByteBuf buffer) {
    }

    public void handle(ServerPlayer sender) {
    }

    public void handle(Minecraft mc) {
    }
}
