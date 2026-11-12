package com.railwayteam.railways.multiloader.fabric;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.zurrtum.create.Create;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PlatformAbstractionHelperImpl {
    public static int getBurnTime(Item item) {
        return getBurnTime(item, Create.SERVER == null ? null : Create.SERVER.overworld());
    }

    public static int getBurnTime(Item item, @Nullable ServerLevel level) {
        if (level == null)
            return item == Items.LAVA_BUCKET ? 20_000 : 0;

        LootContext context = new LootContext.Builder(
            new LootParams.Builder(level).create(LootContextParamSets.EMPTY)
        ).create(Optional.empty());
        return ResolvableInt.getFromItem(item.getDefaultInstance(), DataComponents.COOKING_FUEL,
            CookingFuel::burnTime, context, 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Enum<T>> ArgumentType<T> enumArgument(Class<T> enumClass) {
        return (ArgumentType) StringArgumentType.word();
    }
}
