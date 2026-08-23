/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin.client;

import com.zurrtum.create.client.content.trains.station.StationRenderer;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = StationRenderer.class, remap = false)
public class MixinStationRendererStateReset {
    @Inject(
        method = "extractRenderState(Lcom/zurrtum/create/content/trains/station/StationBlockEntity;Lcom/zurrtum/create/client/content/trains/station/StationRenderer$StationRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
        at = @At("HEAD")
    )
    private void railways$resetRenderState(StationBlockEntity blockEntity,
                                            StationRenderer.StationRenderState state,
                                            float tickProgress, Vec3 cameraPosition,
                                            CrumblingOverlay crumblingOverlay, CallbackInfo ci) {
        state.flag = null;
        state.flagYRot = null;
        state.flagOffsetZ = 0.0f;
        state.flagXRot = null;
        state.flagYRot2 = null;
        state.block = null;
    }
}
