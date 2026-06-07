package com.tterrag.registrate.builders;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public class ItemBuilder<T extends Item, P> extends AbstractBuilder<T, P, ItemBuilder<T, P>> {
    private final Function<Item.Properties, T> factory;
    private Function<Item.Properties, Item.Properties> properties = Function.identity();

    public ItemBuilder(Registrate owner, String name, Function<Item.Properties, T> factory, P parent) {
        super(owner, name, parent);
        this.factory = factory;
    }

    public ItemBuilder<T, P> properties(Function<Item.Properties, Item.Properties> transformer) {
        this.properties = this.properties.andThen(transformer);
        return this;
    }

    public ItemBuilder<T, P> tab(ResourceKey<CreativeModeTab> tab) {
        return this;
    }

    public ItemBuilder<T, P> removeTab(ResourceKey<CreativeModeTab> tab) {
        return this;
    }

    public ItemEntry<T> register() {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, owner.id(name));
        T item = factory.apply(properties.apply(new Item.Properties()).setId(key));
        owner.registerVanilla(BuiltInRegistries.ITEM, name, item);
        return new ItemEntry<>(owner.id(name), item);
    }
}
