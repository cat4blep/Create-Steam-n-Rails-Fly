package com.railwayteam.railways.internal.compat.create.foundation.data;

import com.railwayteam.railways.internal.compat.registrate.builders.BlockBuilder;
import com.railwayteam.railways.internal.compat.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.world.level.block.Block;

public class BuilderTransformers {
    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> copycat() {
        return builder -> builder;
    }
}
