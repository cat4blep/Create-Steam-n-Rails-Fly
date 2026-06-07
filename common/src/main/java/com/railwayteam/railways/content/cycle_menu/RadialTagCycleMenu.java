package com.railwayteam.railways.content.cycle_menu;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RadialTagCycleMenu extends Screen {
    RadialTagCycleMenu(TagKey<Item> tag, List<Item> cycle, @Nullable CompoundTag stackTag) {
        super(Component.empty());
    }
}
