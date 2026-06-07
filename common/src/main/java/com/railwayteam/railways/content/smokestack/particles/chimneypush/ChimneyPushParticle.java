package com.railwayteam.railways.content.smokestack.particles.chimneypush;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public class ChimneyPushParticle {
    public static class Factory<T extends ChimneyPushParticleData<T>> implements ParticleProvider<T> {
        public Factory(SpriteSet spriteSet) {
        }

        public @Nullable Particle createParticle(T type, ClientLevel level, double x, double y, double z,
                                                 double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return null;
        }
    }
}
