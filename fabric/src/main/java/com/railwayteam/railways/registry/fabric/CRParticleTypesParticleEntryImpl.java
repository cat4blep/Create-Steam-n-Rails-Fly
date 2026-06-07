package com.railwayteam.railways.registry.fabric;

import com.zurrtum.create.foundation.particle.ICustomParticleData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

import java.util.function.Supplier;

public class CRParticleTypesParticleEntryImpl {
    public static void register(String id, Supplier<ParticleType<?>> supplier) {
    }

    public static void register() {
    }

    @Environment(EnvType.CLIENT)
    public static <T extends ParticleOptions> void registerFactory(ParticleType<T> object, ParticleEngine engine, ICustomParticleData<T> customParticleData) {
    }
}
