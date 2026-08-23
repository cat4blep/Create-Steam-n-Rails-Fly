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

import com.railwayteam.railways.content.custom_tracks.phantom.PhantomSpriteManager;
import com.railwayteam.railways.mixin_interfaces.IPhantomAnimationState;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteContents.AnimationState.class)
public abstract class MixinSpriteContentsAnimationState implements IPhantomAnimationState {
    @Shadow
    private int frame;

    @Shadow
    private int subFrame;

    @Shadow
    private boolean isDirty;

    @Unique
    private boolean railways$phantom;

    @Unique
    private boolean railways$uploadPending;

    @Override
    @Unique
    public void railways$setPhantom() {
        railways$phantom = true;
        frame = PhantomSpriteManager.isVisible() ? 0 : 1;
        subFrame = 0;
        isDirty = true;
        railways$uploadPending = true;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void railways$holdPhantomFrame(CallbackInfo ci) {
        if (!railways$phantom)
            return;

        int targetFrame = PhantomSpriteManager.isVisible() ? 0 : 1;
        if (frame != targetFrame) {
            frame = targetFrame;
            subFrame = 0;
            railways$uploadPending = true;
        }

        isDirty = railways$uploadPending;
        railways$uploadPending = false;
        ci.cancel();
    }
}
