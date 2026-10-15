/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.content.shadow_realm.ShadowRealm;
import com.railwayteam.railways.content.shadow_realm.ShadowRealm.RestorationTarget;
import com.zurrtum.create.AllHandle;
import com.zurrtum.create.infrastructure.packet.c2s.TrainRelocationPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Restores stored shadow trains on the server thread before Create looks for a live entity. */
@Mixin(value = AllHandle.class, priority = 500, remap = false)
public abstract class MixinAllHandleTrainRelocationShadow {
    @Inject(
        method = "onTrainRelocation",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread("
                + "Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;"
                + "Lnet/minecraft/network/PacketProcessor;)V",
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    private static void railways$restoreShadowTrain(
        ServerGamePacketListenerImpl listener, TrainRelocationPacket packet, CallbackInfo ci
    ) {
        RestorationTarget target = new RestorationTarget(
            listener.player.level(),
            packet.pos(),
            packet.hoveredBezier(),
            packet.direction(),
            packet.lookAngle()
        );
        ShadowRealm.handleTrainRelocationPacket(listener.player, packet.trainId(), target, ci);
    }
}
