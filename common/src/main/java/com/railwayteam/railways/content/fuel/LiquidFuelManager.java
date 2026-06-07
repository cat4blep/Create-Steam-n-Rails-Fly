package com.railwayteam.railways.content.fuel;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class LiquidFuelManager {
    private static final Map<Identifier, LiquidFuelType> CUSTOM_TYPE_MAP = new HashMap<>();
    private static final Map<Fluid, LiquidFuelType> FLUID_TO_TYPE_MAP = new IdentityHashMap<>();
    private static final Map<TagKey<Fluid>, LiquidFuelType> TAG_TO_TYPE_MAP = new IdentityHashMap<>();

    public static void clear() {
        CUSTOM_TYPE_MAP.clear();
        FLUID_TO_TYPE_MAP.clear();
        TAG_TO_TYPE_MAP.clear();
    }

    public static LiquidFuelType getTypeForFluid(Fluid fluid) {
        return FLUID_TO_TYPE_MAP.get(fluid);
    }

    @Nullable
    public static LiquidFuelType isInTag(Fluid fluid) {
        for (Map.Entry<TagKey<Fluid>, LiquidFuelType> entry : TAG_TO_TYPE_MAP.entrySet()) {
            if (fluid.defaultFluidState().is(entry.getKey()))
                return entry.getValue();
        }
        return null;
    }

    public static void fillFluidMap() {
    }

    public static class ReloadListener {
        public static final ReloadListener INSTANCE = new ReloadListener();
        public static final String ID = "railways_liquid_fuel";

        protected ReloadListener() {
        }
    }
}
