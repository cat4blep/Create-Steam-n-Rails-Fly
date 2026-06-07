/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.custom_tracks.casing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.trains.track.BezierConnection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public abstract class CasingRenderUtils {
    public static void clearModelCache() {
    }

    public static PartialModel reTexture(PartialModel model, Block block) {
        return model;
    }

    public static void renderBezierCasings(PoseStack ms, Level level, PartialModel texturedPartial, BlockState state, VertexConsumer vb, BezierConnection bc) {
    }

    public static List<Vec3> casingPositions(BezierConnection bc) {
        return List.of();
    }

    public static TransformedInstance makeCasingInstance(PartialModel baseModel, Block casingBlock, InstancerProvider instancerProvider) {
        return null;
    }
}
