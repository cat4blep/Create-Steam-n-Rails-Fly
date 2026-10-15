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

import com.railwayteam.railways.content.custom_tracks.phantom.PhantomTrackOverlays;
import com.zurrtum.create.client.content.trains.track.StandardTrackBlockRenderer;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderState;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.client.flywheel.lib.transform.Affine;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = StandardTrackBlockRenderer.class, remap = false)
public class MixinStandardTrackBlockRendererPhantom {
    @Inject(method = "prepareTrackOverlay", at = @At("HEAD"), cancellable = true)
    private void railways$hideInstancedPhantomOverlay(Affine<?> affine, BlockGetter world, BlockPos pos,
                                                       BlockState state, BezierTrackPointLocation bezierPoint,
                                                       AxisDirection direction, RenderedTrackOverlayType type,
                                                       CallbackInfo ci) {
        if (PhantomTrackOverlays.isHidden(world, pos, state, bezierPoint)) {
            affine.scale(0);
            ci.cancel();
        }
    }

    @Inject(
        method = "getRenderState(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction$AxisDirection;Lcom/zurrtum/create/infrastructure/component/BezierTrackPointLocation;Lcom/zurrtum/create/content/trains/track/TrackTargetingBehaviour$RenderedTrackOverlayType;F)Lcom/zurrtum/create/client/content/trains/track/TrackBlockRenderState;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void railways$hidePhantomOverlay(Level level, Vec3 offset, BlockState state, BlockPos pos,
                                              AxisDirection direction, BezierTrackPointLocation bezierPoint,
                                              RenderedTrackOverlayType type, float scale,
                                              CallbackInfoReturnable<TrackBlockRenderState> cir) {
        if (PhantomTrackOverlays.isHidden(level, pos, state, bezierPoint))
            cir.setReturnValue(PhantomTrackOverlays.EMPTY);
    }

    @Inject(
        method = "getAssemblyRenderState(Lcom/zurrtum/create/content/trains/station/StationBlockEntity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lcom/zurrtum/create/client/content/trains/track/TrackBlockRenderState;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void railways$hidePhantomAssemblyOverlay(StationBlockEntity station, Vec3 offset, Level level,
                                                      BlockPos pos, BlockState state,
                                                      CallbackInfoReturnable<TrackBlockRenderState> cir) {
        if (PhantomTrackOverlays.isHidden(level, pos, state, null))
            cir.setReturnValue(PhantomTrackOverlays.EMPTY);
    }
}
