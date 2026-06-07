package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.CreativeModeTab;

import java.util.function.Supplier;

public class CRCreativeModeTabs {
    @ExpectPlatform
    public static ResourceKey<CreativeModeTab> getBaseTabKey() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ResourceKey<CreativeModeTab> getTracksTabKey() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ResourceKey<CreativeModeTab> getPalettesTabKey() {
        throw new AssertionError();
    }

    public static void register() {
    }

    public enum Tabs {
        MAIN(CRCreativeModeTabs::getBaseTabKey),
        TRACK(CRCreativeModeTabs::getTracksTabKey),
        PALETTES(CRCreativeModeTabs::getPalettesTabKey);

        private final Supplier<ResourceKey<CreativeModeTab>> keySupplier;

        Tabs(Supplier<ResourceKey<CreativeModeTab>> keySupplier) {
            this.keySupplier = keySupplier;
        }

        public ResourceKey<CreativeModeTab> getKey() {
            return keySupplier.get();
        }
    }

    public static final class RegistrateDisplayItemsGenerator implements CreativeModeTab.DisplayItemsGenerator {
        private final Tabs tab;

        public RegistrateDisplayItemsGenerator(Tabs tab) {
            this.tab = tab;
        }

        @Override
        public void accept(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
            for (var entry : Railways.registrate().getAll(Registries.ITEM)) {
                if (!isInCreativeTab(entry, tab.getKey()))
                    continue;
                Item item = entry.get();
                if (item != Items.AIR)
                    output.accept(new ItemStack(item));
            }
        }
    }

    @ExpectPlatform
    private static boolean isInCreativeTab(com.tterrag.registrate.util.entry.RegistryEntry<?> entry, ResourceKey<CreativeModeTab> tab) {
        throw new AssertionError();
    }

    public record TabInfo(ResourceKey<CreativeModeTab> key, CreativeModeTab tab) {
    }

    public static ResourceKey<CreativeModeTab> key(String name) {
        return ResourceKey.create(Registries.CREATIVE_MODE_TAB, Railways.asResource(name));
    }
}
