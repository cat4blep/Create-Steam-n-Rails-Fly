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

package com.railwayteam.railways.fabric_mixin.client;

import com.railwayteam.railways.content.palettes.doors.PalettesSlidingDoorBlock;
import com.railwayteam.railways.content.palettes.doors.PalettesSlidingDoorVisual;
import com.zurrtum.create.client.content.decoration.slidingDoor.SlidingDoorVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SlidingDoorVisual.class)
public abstract class MixinSlidingDoorVisual {
    @Inject(
        method = "create(Lcom/zurrtum/create/client/flywheel/api/visualization/VisualizationContext;Lcom/zurrtum/create/content/decoration/slidingDoor/SlidingDoorBlockEntity;F)Lcom/zurrtum/create/client/content/decoration/slidingDoor/SlidingDoorVisual;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void railways$createPaletteVisual(
        VisualizationContext context,
        SlidingDoorBlockEntity blockEntity,
        float partialTick,
        CallbackInfoReturnable<SlidingDoorVisual> cir
    ) {
        if (blockEntity.getBlockState().getBlock() instanceof PalettesSlidingDoorBlock) {
            cir.setReturnValue(PalettesSlidingDoorVisual.create(context, blockEntity, partialTick));
        }
    }
}
