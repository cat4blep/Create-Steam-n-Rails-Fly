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

import com.railwayteam.railways.content.custom_tracks.phantom.PhantomSpriteManager;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.client.content.trains.track.StandardTrackBlockRenderer;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderState;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = StandardTrackBlockRenderer.class, remap = false)
public class MixinStandardTrackBlockRendererPhantom {
    @Inject(
        method = "getRenderState(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction$AxisDirection;Lcom/zurrtum/create/infrastructure/component/BezierTrackPointLocation;Lcom/zurrtum/create/content/trains/track/TrackTargetingBehaviour$RenderedTrackOverlayType;F)Lcom/zurrtum/create/client/content/trains/track/TrackBlockRenderState;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void railways$hidePhantomOverlay(Level level, Vec3 offset, BlockState state, BlockPos pos,
                                              AxisDirection direction, BezierTrackPointLocation bezierPoint,
                                              RenderedTrackOverlayType type, float scale,
                                              CallbackInfoReturnable<TrackBlockRenderState> cir) {
        if (railways$isHiddenPhantom(level, pos, state, bezierPoint))
            cir.setReturnValue(null);
    }

    @Inject(
        method = "getAssemblyRenderState(Lcom/zurrtum/create/content/trains/station/StationBlockEntity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lcom/zurrtum/create/client/content/trains/track/TrackBlockRenderState;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void railways$hidePhantomAssemblyOverlay(StationBlockEntity station, Vec3 offset, Level level,
                                                      BlockPos pos, BlockState state,
                                                      CallbackInfoReturnable<TrackBlockRenderState> cir) {
        if (railways$isHiddenPhantom(level, pos, state, null))
            cir.setReturnValue(null);
    }

    private static boolean railways$isHiddenPhantom(Level level, BlockPos pos, BlockState state,
                                                     BezierTrackPointLocation bezierPoint) {
        if (PhantomSpriteManager.isVisible())
            return false;

        if (bezierPoint == null)
            return state.getBlock() instanceof TrackBlock track
                && track.getMaterial() == CRTrackMaterials.PHANTOM;

        if (!(level.getBlockEntity(pos) instanceof TrackBlockEntity trackEntity))
            return false;

        BezierConnection connection = trackEntity.getConnections().get(bezierPoint.curveTarget());
        return connection != null && connection.getMaterial() == CRTrackMaterials.PHANTOM;
    }
}
