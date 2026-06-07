package com.tterrag.registrate.builders;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.MenuEntry;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class MenuBuilder<T extends AbstractContainerMenu, P> extends AbstractBuilder<MenuType<T>, P, MenuBuilder<T, P>> {
    @FunctionalInterface
    public interface ForgeMenuFactory<T extends AbstractContainerMenu> {
        T create(MenuType<T> type, int id, Inventory inventory, FriendlyByteBuf buffer);
    }

    @FunctionalInterface
    public interface ScreenFactory<C extends AbstractContainerMenu, S> {
        S create(C menu, Inventory inventory, net.minecraft.network.chat.Component title);
    }

    private final NonNullBiFunction<MenuType<T>, Integer, T> factory;

    public MenuBuilder(Registrate owner, String name, NonNullBiFunction<MenuType<T>, Integer, T> factory) {
        super(owner, name, null);
        this.factory = factory;
    }

    public MenuBuilder<T, P> screen(java.util.function.Supplier<?> screenFactory) {
        return this;
    }

    public MenuEntry<T> register() {
        MenuType<T> type = new MenuType<>((syncId, inventory) -> factory.apply(null, syncId), net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS);
        owner.registerVanilla(BuiltInRegistries.MENU, name, type);
        return new MenuEntry<>(owner.id(name), type);
    }
}
