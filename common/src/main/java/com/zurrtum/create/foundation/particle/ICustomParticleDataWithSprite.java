package com.zurrtum.create.foundation.particle;

import net.minecraft.core.particles.ParticleOptions;

public interface ICustomParticleDataWithSprite<T extends ParticleOptions> extends ICustomParticleData<T> {
    Object getMetaFactory();
}
