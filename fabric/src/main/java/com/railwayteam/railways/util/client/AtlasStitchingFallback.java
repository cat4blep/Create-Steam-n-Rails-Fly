/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.util.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.StitcherException;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.IntFunction;

@Environment(EnvType.CLIENT)
public final class AtlasStitchingFallback {
    private AtlasStitchingFallback() {}

    public record Result<T>(T value, int mipLevel) {}

    public static <T> Result<T> stitch(Identifier atlas, List<? extends Stitcher.Entry> sprites,
                                      int requestedMipLevel, IntFunction<T> attempt) {
        boolean railwaysBlocks = TextureAtlas.LOCATION_BLOCKS.equals(atlas)
            && sprites.stream().anyMatch(sprite -> sprite.name().getNamespace().equals("railways"));
        int mipLevel = requestedMipLevel;
        while (true) {
            try {
                return new Result<>(attempt.apply(mipLevel), mipLevel);
            } catch (RuntimeException error) {
                boolean cannotFit = error instanceof StitcherException
                    || (error instanceof ReportedException && error.getCause() instanceof StitcherException);
                if (!railwaysBlocks || !cannotFit || mipLevel <= 0)
                    throw error;
                // 26.2 adds padding proportional to both mip level and anisotropy
                // to every sprite. Retry the complete vanilla preparation so the
                // packed coordinates, padding, mip images and upload agree.
                // Pixel resolution and the user's global options stay unchanged.
                mipLevel--;
            }
        }
    }
}
