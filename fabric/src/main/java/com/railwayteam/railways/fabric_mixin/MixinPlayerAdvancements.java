/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.internal.datafix.PlayerAdvancementsJsonRepair;
import com.railwayteam.railways.internal.datafix.PlayerAdvancementsJsonRepair.RepairResult;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerAdvancements.class)
public abstract class MixinPlayerAdvancements {
    @ModifyExpressionValue(
        method = "load(Lnet/minecraft/server/ServerAdvancementManager;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/StrictJsonParser;parse(Ljava/io/Reader;)Lcom/google/gson/JsonElement;"
        ),
        require = 1
    )
    private JsonElement railways$repairLegacyRootMarker(JsonElement parsed) {
        RepairResult result = PlayerAdvancementsJsonRepair.repair(parsed);
        if (result.repaired()) {
            Railways.LOGGER.warn(
                "Recovered player advancement progress by removing the legacy root marker {}. The repaired data will be saved normally.",
                PlayerAdvancementsJsonRepair.LEGACY_DATA_VERSION_KEY
            );
        }
        return result.value();
    }
}
