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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.zurrtum.create.client.catnip.render.PonderRenderPipelines;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PonderRenderTypes.class)
public abstract class MixinPonderRenderTypes {
    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/client/renderer/rendertype/RenderSetup;builder(Lcom/mojang/blaze3d/pipeline/RenderPipeline;)Lnet/minecraft/client/renderer/rendertype/RenderSetup$RenderSetupBuilder;"))
    private static RenderSetup.RenderSetupBuilder railways$bindEntityOverlay(RenderPipeline pipeline,
            Operation<RenderSetup.RenderSetupBuilder> original) {
        RenderSetup.RenderSetupBuilder builder = original.call(pipeline);
        if (pipeline == PonderRenderPipelines.ENTITY_BLOCK_SOLID
            || pipeline == PonderRenderPipelines.ENTITY_BLOCK_CUTOUT
            || pipeline == PonderRenderPipelines.ENTITY_BLOCK_TRANSLUCENT
            || pipeline == PonderRenderPipelines.NETHER_ENTITY_BLOCK_SOLID
            || pipeline == PonderRenderPipelines.NETHER_ENTITY_BLOCK_CUTOUT
            || pipeline == PonderRenderPipelines.NETHER_ENTITY_BLOCK_TRANSLUCENT) {
            // Iris consumes UV1 as the entity overlay. Without Sampler1 it samples a stale texture
            // from an earlier draw, so culling nearby geometry can turn entire contraptions white.
            builder.useOverlay();
        }
        return builder;
    }
}
