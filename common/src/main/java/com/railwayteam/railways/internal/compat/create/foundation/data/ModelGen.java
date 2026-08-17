package com.railwayteam.railways.internal.compat.create.foundation.data;

import com.railwayteam.railways.internal.compat.registrate.builders.ItemBuilder;
import com.railwayteam.railways.internal.compat.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.world.item.Item;

public class ModelGen {
    public static <I extends Item, P> NonNullUnaryOperator<ItemBuilder<I, P>> customItemModel() {
        return NonNullUnaryOperator.identity();
    }

    public static <I extends Item, P> NonNullUnaryOperator<ItemBuilder<I, P>> customItemModel(String path) {
        return NonNullUnaryOperator.identity();
    }

    public static <I extends Item, P> NonNullUnaryOperator<ItemBuilder<I, P>> customItemModel(String prefix, String suffix) {
        return NonNullUnaryOperator.identity();
    }
}
