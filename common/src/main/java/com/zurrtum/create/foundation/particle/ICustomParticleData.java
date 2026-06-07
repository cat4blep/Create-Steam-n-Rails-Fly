package com.zurrtum.create.foundation.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface ICustomParticleData<T extends ParticleOptions> {
    interface Deserializer<T extends ParticleOptions> {
        T fromCommand(ParticleType<T> type, StringReader reader) throws CommandSyntaxException;

        T fromNetwork(ParticleType<T> type, FriendlyByteBuf buffer);
    }

    Deserializer<T> getDeserializer();

    Codec<T> getCodec(ParticleType<T> type);

    default ParticleType<T> createType() {
        return new ParticleType<T>(false) {
            public MapCodec<T> codec() {
                return null;
            }
            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return null;
            }
        };
    }
}
