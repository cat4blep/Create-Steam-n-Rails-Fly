package com.railwayteam.railways.content.cycle_menu;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TagCycleTracker {
    public void registerCycle(TagKey<Item> tag) {
    }

    public void scheduleRecompute() {
    }

    public void computeCycles() {
    }

    public @Nullable TagKey<Item> getCycleTag(Item item) {
        return null;
    }

    public List<Item> getCycle(TagKey<Item> tag) {
        return List.of();
    }
}
