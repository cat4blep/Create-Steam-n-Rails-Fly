/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.content.palettes.doors;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.decoration.slidingDoor.SlidingDoorRenderer;
import com.zurrtum.create.client.content.decoration.slidingDoor.SlidingDoorRenderer.DoorRenderState;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlockEntity;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import org.joml.Quaternionf;
import org.joml.Vector3fc;

import java.util.Objects;

/** Shader-compatible block entity render state for two-piece palette sliding doors. */
public final class PalettesSlidingDoorRenderState implements SlidingDoorRenderer.AbstractDoorRenderState {
    private final SuperByteBufferRenderState bottom;
    private final SuperByteBufferRenderState top;
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;
    private final Quaternionf modelRotation;

    private PalettesSlidingDoorRenderState(
        SuperByteBufferRenderState bottom,
        SuperByteBufferRenderState top,
        float offsetX,
        float offsetY,
        float offsetZ,
        Quaternionf modelRotation
    ) {
        this.bottom = bottom;
        this.top = top;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.modelRotation = modelRotation;
    }

    public static PalettesSlidingDoorRenderState create(
        SlidingDoorBlockEntity blockEntity,
        DoorRenderState state,
        float partialTick,
        PalettesSlidingDoorBlock block,
        SuperByteBufferRenderState bottom
    ) {
        Level level = Objects.requireNonNull(blockEntity.getLevel(), "Palette door is not in a level");
        BlockState blockState = blockEntity.getBlockState();
        Direction facing = blockState.getValue(DoorBlock.FACING);
        boolean windowed = blockState.getValue(PalettesSlidingDoorBlock.WINDOWED);
        Couple<PartialModel> partials = CRBlockPartials.SLIDING_DOORS.get(block.color).get(windowed);

        // Create has already extracted the bottom model using its SOUTH-facing
        // convention. Rotate both halves by the delta to Railway's EAST-facing
        // convention, while retaining that state so its pooled render data is
        // submitted and recycled normally.
        float desiredYaw = AngleHelper.horizontalAngle(facing.getClockWise());
        float extractedYaw = AngleHelper.horizontalAngle(facing);
        Quaternionf modelRotation = new Quaternionf().rotationY(Mth.DEG_TO_RAD * (desiredYaw - extractedYaw));
        SuperByteBufferRenderState top = CachedBuffers
            .partialFacing(partials.get(true), blockState, facing)
            .cardinalLighting(level)
            .light(LightCoordsUtil.getLightCoords(level, blockEntity.getBlockPos().above()))
            .extractRenderState();

        boolean left = blockState.getValue(DoorBlock.HINGE) == DoorHingeSide.LEFT;
        float value = blockEntity.animation.getValue(partialTick);
        Vec3i facingVector = facing.getUnitVec3i();
        Vector3fc movementVector = left
            ? facing.getCounterClockWise().getUnitVec3f()
            : facing.getClockWise().getUnitVec3f();
        float facingScale = Mth.clamp(value * 10, 0, 1) * 0.03125f;
        float movementScale = value * value * 0.8125f;
        float offsetX = Math.fma(movementVector.x(), movementScale, facingVector.getX() * facingScale);
        float offsetY = Math.fma(
            movementVector.y(),
            movementScale,
            Math.fma(facingVector.getY(), facingScale, SlidingDoorRenderer.DOOR_OFFSET)
        );
        float offsetZ = Math.fma(movementVector.z(), movementScale, facingVector.getZ() * facingScale);
        return new PalettesSlidingDoorRenderState(bottom, top, offsetX, offsetY, offsetZ, modelRotation);
    }

    @Override
    public void submit(PoseStack matrices, OrderedSubmitNodeCollector queue) {
        matrices.pushPose();
        matrices.translate(offsetX, offsetY, offsetZ);
        matrices.rotateAround(modelRotation, 0.5f, 0.5f, 0.5f);
        bottom.submit(matrices, queue);
        matrices.translate(0, 1, 0);
        top.submit(matrices, queue);
        matrices.popPose();
    }
}
