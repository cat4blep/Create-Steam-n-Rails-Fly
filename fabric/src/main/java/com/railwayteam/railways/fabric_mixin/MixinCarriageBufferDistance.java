/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
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

package com.railwayteam.railways.fabric_mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.railwayteam.railways.mixin_interfaces.ICarriageBufferDistanceTracker;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Carriage.class, remap = false)
public abstract class MixinCarriageBufferDistance implements ICarriageBufferDistanceTracker {
    @Unique
    private @Nullable Integer railways$leadingBufferDistance = null;

    @Unique
    private @Nullable Integer railways$trailingBufferDistance = null;

    @Override
    public @Nullable Integer railways$getLeadingDistance() {
        return railways$leadingBufferDistance;
    }

    @Override
    public @Nullable Integer railways$getTrailingDistance() {
        return railways$trailingBufferDistance;
    }

    @Override
    public void railways$setLeadingDistance(int distance) {
        railways$leadingBufferDistance = Math.max(0, distance);
    }

    @Override
    public void railways$setTrailingDistance(int distance) {
        railways$trailingBufferDistance = Math.max(0, distance);
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void railways$writeBufferDistances(ValueOutput output, DimensionPalette dimensions, CallbackInfo ci) {
        if (railways$leadingBufferDistance != null)
            output.putInt("LeadingBufferDistance", railways$leadingBufferDistance);
        if (railways$trailingBufferDistance != null)
            output.putInt("TrailingBufferDistance", railways$trailingBufferDistance);
    }

    @Inject(method = "read", at = @At("RETURN"))
    private static void railways$readBufferDistances(ValueInput input, TrackGraph graph, DimensionPalette dimensions,
                                                      CallbackInfoReturnable<Carriage> cir) {
        ICarriageBufferDistanceTracker tracker = (ICarriageBufferDistanceTracker) cir.getReturnValue();
        input.getInt("LeadingBufferDistance")
            .ifPresent(tracker::railways$setLeadingDistance);
        input.getInt("TrailingBufferDistance")
            .ifPresent(tracker::railways$setTrailingDistance);
    }

    @WrapOperation(
        method = "encode",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/serialization/DynamicOps;mapBuilder()Lcom/mojang/serialization/RecordBuilder;",
            ordinal = 0
        )
    )
    private static <T> RecordBuilder<T> railways$encodeBufferDistances(
        DynamicOps<T> ops,
        Operation<RecordBuilder<T>> original,
        @Local(argsOnly = true) Carriage carriage
    ) {
        RecordBuilder<T> builder = original.call(ops);
        ICarriageBufferDistanceTracker tracker = (ICarriageBufferDistanceTracker) carriage;
        Integer leadingDistance = tracker.railways$getLeadingDistance();
        Integer trailingDistance = tracker.railways$getTrailingDistance();
        if (leadingDistance != null)
            builder.add("LeadingBufferDistance", ops.createInt(leadingDistance));
        if (trailingDistance != null)
            builder.add("TrailingBufferDistance", ops.createInt(trailingDistance));
        return builder;
    }

    @Inject(method = "decode", at = @At("RETURN"))
    private static <T> void railways$decodeBufferDistances(
        DynamicOps<T> ops,
        T input,
        TrackGraph graph,
        DimensionPalette dimensions,
        CallbackInfoReturnable<Carriage> cir
    ) {
        ICarriageBufferDistanceTracker tracker = (ICarriageBufferDistanceTracker) cir.getReturnValue();
        MapLike<T> map = ops.getMap(input).getOrThrow();
        T leadingDistance = map.get("LeadingBufferDistance");
        T trailingDistance = map.get("TrailingBufferDistance");
        if (leadingDistance != null)
            ops.getNumberValue(leadingDistance).result().map(Number::intValue)
                .ifPresent(tracker::railways$setLeadingDistance);
        if (trailingDistance != null)
            ops.getNumberValue(trailingDistance).result().map(Number::intValue)
                .ifPresent(tracker::railways$setTrailingDistance);
    }
}
