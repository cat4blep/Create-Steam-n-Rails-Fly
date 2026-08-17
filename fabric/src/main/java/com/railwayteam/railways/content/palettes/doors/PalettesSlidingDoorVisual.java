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

import com.railwayteam.railways.registry.CRBlockPartials;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.content.decoration.slidingDoor.SlidingDoorRenderer;
import com.zurrtum.create.client.content.decoration.slidingDoor.SlidingDoorVisual;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Flywheel visuals for palette doors. Create Fly only registers models for its
 * own door ids, so addon doors must supply their instances directly.
 */
public abstract class PalettesSlidingDoorVisual extends SlidingDoorVisual {
    protected PalettesSlidingDoorVisual(
        VisualizationContext context,
        SlidingDoorBlockEntity blockEntity,
        float partialTick
    ) {
        super(context, blockEntity, partialTick);
    }

    public static SlidingDoorVisual create(
        VisualizationContext context,
        SlidingDoorBlockEntity blockEntity,
        float partialTick
    ) {
        PalettesSlidingDoorBlock block = (PalettesSlidingDoorBlock) blockEntity.getBlockState().getBlock();
        if (block.isFoldingDoor()) {
            return new Folding(context, blockEntity, partialTick, block);
        }
        return new Sliding(context, blockEntity, partialTick, block);
    }

    private static final class Folding extends PalettesSlidingDoorVisual {
        private final TransformedInstance left;
        private final TransformedInstance right;
        private final Vector3fc facingVector;
        private final boolean flip;
        private final float angle;

        private Folding(
            VisualizationContext context,
            SlidingDoorBlockEntity blockEntity,
            float partialTick,
            PalettesSlidingDoorBlock block
        ) {
            super(context, blockEntity, partialTick);
            flip = blockState.getValue(DoorBlock.HINGE) != DoorHingeSide.LEFT;
            Direction facing = blockState.getValue(DoorBlock.FACING);
            facingVector = facing.getUnitVec3f();
            angle = Mth.DEG_TO_RAD * AngleHelper.horizontalAngle(facing.getClockWise());

            boolean windowed = blockState.getValue(PalettesSlidingDoorBlock.WINDOWED);
            Couple<PartialModel> partials = CRBlockPartials.FOLDING_DOORS.get(block.color).get(windowed);
            InstancerProvider instancers = instancerProvider();
            left = instancers.instancer(InstanceTypes.TRANSFORMED, Models.chunkPartial(partials.get(!flip)))
                .createInstance();
            right = instancers.instancer(InstanceTypes.TRANSFORMED, Models.chunkPartial(partials.get(flip)))
                .createInstance();
            transformModels(partialTick);
        }

        @Override
        protected void transformModels(float partialTick) {
            float value = blockEntity.animation.getValue(partialTick);
            BlockPos position = getVisualPosition();
            if (value != 0) {
                float squaredValue = value * value;
                float scale = Mth.clamp(value * 10, 0, 1) * 0.03125f;
                left.setIdentityTransform().translate(
                    Math.fma(facingVector.x(), scale, position.getX()),
                    Math.fma(facingVector.y(), scale, position.getY() + SlidingDoorRenderer.DOOR_OFFSET),
                    Math.fma(facingVector.z(), scale, position.getZ())
                ).rotateCentered(angle, Direction.UP);
                if (flip) {
                    left.translate(0, 0, 1).rotateYDegrees(-91 * squaredValue).translate(0, 0, -0.5f);
                    right.setTransform(left.pose).rotateYDegrees(181 * squaredValue).translate(0, 0, -0.5f);
                } else {
                    left.rotateYDegrees(91 * squaredValue);
                    right.setTransform(left.pose).translate(0, 0, 0.5f).rotateYDegrees(-181 * squaredValue);
                }
            } else {
                left.setIdentityTransform()
                    .translate(position.getX(), position.getY() + SlidingDoorRenderer.DOOR_OFFSET, position.getZ())
                    .rotateCentered(angle, Direction.UP);
                if (flip) {
                    right.setTransform(left.pose);
                    left.translate(0, 0, 0.5f);
                } else {
                    right.setTransform(left.pose).translate(0, 0, 0.5f);
                }
            }
            left.setChanged();
            right.setChanged();
        }

        @Override
        public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
            consumer.accept(left);
            consumer.accept(right);
        }

        @Override
        public void updateLight(float partialTick) {
            relight(left, right);
        }

        @Override
        protected void _delete() {
            left.delete();
            right.delete();
        }
    }

    private static final class Sliding extends PalettesSlidingDoorVisual {
        private final TransformedInstance bottom;
        private final TransformedInstance top;
        private final Vector3fc facingVector;
        private final Vector3fc movementVector;
        private final float angle;

        private Sliding(
            VisualizationContext context,
            SlidingDoorBlockEntity blockEntity,
            float partialTick,
            PalettesSlidingDoorBlock block
        ) {
            super(context, blockEntity, partialTick);
            boolean windowed = blockState.getValue(PalettesSlidingDoorBlock.WINDOWED);
            Couple<PartialModel> partials = CRBlockPartials.SLIDING_DOORS.get(block.color).get(windowed);
            InstancerProvider instancers = instancerProvider();
            bottom = instancers.instancer(InstanceTypes.TRANSFORMED, Models.chunkPartial(partials.get(false)))
                .createInstance();
            top = instancers.instancer(InstanceTypes.TRANSFORMED, Models.chunkPartial(partials.get(true)))
                .createInstance();

            Direction facing = blockState.getValue(DoorBlock.FACING);
            facingVector = facing.getUnitVec3f();
            movementVector = blockState.getValue(DoorBlock.HINGE) == DoorHingeSide.LEFT
                ? facing.getCounterClockWise().getUnitVec3f()
                : facing.getClockWise().getUnitVec3f();
            angle = Mth.DEG_TO_RAD * AngleHelper.horizontalAngle(facing.getClockWise());
            transformModels(partialTick);
        }

        @Override
        protected void transformModels(float partialTick) {
            float value = blockEntity.animation.getValue(partialTick);
            BlockPos position = getVisualPosition();
            float x = position.getX();
            float y = position.getY();
            float z = position.getZ();
            if (value != 0) {
                float facingScale = Mth.clamp(value * 10, 0, 1) * 0.03125f;
                float movementScale = value * value * 0.8125f;
                x = Math.fma(movementVector.x(), movementScale,
                    Math.fma(facingVector.x(), facingScale, x));
                y = Math.fma(movementVector.y(), movementScale,
                    Math.fma(facingVector.y(), facingScale, y + SlidingDoorRenderer.DOOR_OFFSET));
                z = Math.fma(movementVector.z(), movementScale,
                    Math.fma(facingVector.z(), facingScale, z));
            }

            bottom.setIdentityTransform().translate(x, y, z).rotateCentered(angle, Direction.UP);
            top.setIdentityTransform().translate(x, y + 1, z).rotateCentered(angle, Direction.UP);
            bottom.setChanged();
            top.setChanged();
        }

        @Override
        public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
            consumer.accept(bottom);
            consumer.accept(top);
        }

        @Override
        public void updateLight(float partialTick) {
            relight(bottom);
            relight(blockEntity.getBlockPos().above(), top);
        }

        @Override
        protected void _delete() {
            bottom.delete();
            top.delete();
        }
    }
}
