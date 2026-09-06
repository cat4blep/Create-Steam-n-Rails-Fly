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

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.util.client.AtlasStitchingFallback;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.concurrent.Executor;

@Mixin(SpriteLoader.class)
public abstract class MixinSpriteLoaderAtlasCapacity {
    @Shadow @Final private Identifier location;
    @Shadow @Final private int maxSupportedTextureSize;

    @WrapMethod(method = "stitch")
    private SpriteLoader.Preparations railways$fitBlockAtlas(List<SpriteContents> sprites, int mipLevel,
            Executor executor, Operation<SpriteLoader.Preparations> original) {
        var result = AtlasStitchingFallback.stitch(location, sprites, mipLevel,
            retryMipLevel -> original.call(sprites, retryMipLevel, executor));
        if (result.mipLevel() != mipLevel) {
            Railways.LOGGER.warn("[Railways] Block atlas exceeded {}x{} at mip level {}; "
                    + "using mip level {} for this reload ({} sprites, original texture resolution retained)",
                maxSupportedTextureSize, maxSupportedTextureSize, mipLevel, result.value().mipLevel(), sprites.size());
        }
        return result.value();
    }
}
