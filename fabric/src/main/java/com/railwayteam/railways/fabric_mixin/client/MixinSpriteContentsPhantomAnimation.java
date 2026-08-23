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

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.railwayteam.railways.content.custom_tracks.phantom.PhantomSpriteManager;
import com.railwayteam.railways.mixin_interfaces.IPhantomAnimationState;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpriteContents.class)
public abstract class MixinSpriteContentsPhantomAnimation {
    @Inject(
        method = "createAnimationState(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;I)Lnet/minecraft/client/renderer/texture/SpriteContents$AnimationState;",
        at = @At("RETURN")
    )
    private void railways$configurePhantomAnimation(GpuBufferSlice allocationBuffer, int offsetMultiplier,
                                                     CallbackInfoReturnable<SpriteContents.AnimationState> cir) {
        SpriteContents contents = (SpriteContents) (Object) this;
        SpriteContents.AnimationState animationState = cir.getReturnValue();
        if (animationState != null && PhantomSpriteManager.isPhantomSprite(contents)
            && contents.getUniqueFrames().contains(0) && contents.getUniqueFrames().contains(1))
            ((IPhantomAnimationState) animationState).railways$setPhantom();
    }
}
