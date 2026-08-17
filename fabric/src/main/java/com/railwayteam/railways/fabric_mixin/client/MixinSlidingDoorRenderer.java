package com.railwayteam.railways.fabric_mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.railwayteam.railways.content.palettes.doors.PalettesSlidingDoorBlock;
import com.railwayteam.railways.content.palettes.doors.PalettesSlidingDoorRenderState;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.content.decoration.slidingDoor.SlidingDoorRenderer;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlockEntity;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(SlidingDoorRenderer.class)
public class MixinSlidingDoorRenderer {
    @WrapOperation(
        method = "extractRenderState(Lcom/zurrtum/create/content/decoration/slidingDoor/SlidingDoorBlockEntity;Lcom/zurrtum/create/client/content/decoration/slidingDoor/SlidingDoorRenderer$DoorRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object getPalettesPartials(@SuppressWarnings("rawtypes") Map instance, Object key, Operation<Object> original,
                                       @Local SlidingDoorBlock block,
                                       @Local(argsOnly = true) SlidingDoorBlockEntity blockEntity) {
        if (block instanceof PalettesSlidingDoorBlock paletteBlock) {
            boolean windowed = blockEntity.getBlockState().getValue(PalettesSlidingDoorBlock.WINDOWED);
            if (paletteBlock.isFoldingDoor()) {
                return CRBlockPartials.FOLDING_DOORS.get(paletteBlock.color).get(windowed);
            }

            // Keep Create's extraction path non-null; the complete two-piece state is
            // installed at TAIL below.
            Couple<?> partials = CRBlockPartials.SLIDING_DOORS.get(paletteBlock.color).get(windowed);
            return partials.get(false);
        }
        return original.call(instance, key);
    }

    @Inject(
        method = "extractRenderState(Lcom/zurrtum/create/content/decoration/slidingDoor/SlidingDoorBlockEntity;Lcom/zurrtum/create/client/content/decoration/slidingDoor/SlidingDoorRenderer$DoorRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
        at = @At("TAIL")
    )
    private void railways$installSlidingRenderState(
        SlidingDoorBlockEntity blockEntity,
        SlidingDoorRenderer.DoorRenderState state,
        float partialTick,
        Vec3 cameraPosition,
        CrumblingOverlay crumblingOverlay,
        CallbackInfo ci
    ) {
        if (blockEntity.getBlockState().getBlock() instanceof PalettesSlidingDoorBlock block && !block.isFoldingDoor()) {
            SlidingDoorRenderer.SlidingDoorRenderState extracted =
                (SlidingDoorRenderer.SlidingDoorRenderState) state.door;
            state.door = PalettesSlidingDoorRenderState.create(
                blockEntity,
                state,
                partialTick,
                block,
                extracted.model
            );
        }
    }
}
