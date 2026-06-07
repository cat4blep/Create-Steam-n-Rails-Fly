package com.railwayteam.railways.content.smokestack.particles.puffs;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public class PuffSmokeParticle {
    public static final double DOUBLE_SPEED_SENTINEL = -42;

    public static class Factory<T extends PuffSmokeParticleData<T>> implements ParticleProvider<T> {
        public Factory(SpriteSet spriteSet) {
        }

        public @Nullable Particle createParticle(T type, ClientLevel level, double x, double y, double z,
                                                 double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return null;
        }
    }
}
