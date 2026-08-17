package com.railwayteam.railways.internal.compat.minecraftforge.fml.config;

public class ModConfig {
    public enum Type {
        CLIENT,
        COMMON,
        SERVER
    }

    public IConfigSpec<?> getSpec() {
        return null;
    }
}
