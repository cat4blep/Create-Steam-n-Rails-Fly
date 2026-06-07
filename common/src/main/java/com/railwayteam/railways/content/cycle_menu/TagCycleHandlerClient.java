package com.railwayteam.railways.content.cycle_menu;

import com.railwayteam.railways.annotation.multiloader.MultiLoaderEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.Item;

@Environment(EnvType.CLIENT)
public class TagCycleHandlerClient {
    public static final TagCycleTracker CYCLE_TRACKER = new TagCycleTracker();
    public static int COOLDOWN = 0;

    @MultiLoaderEvent
    public static void clientTick() {
    }

    static void select(Item target) {
    }

    @MultiLoaderEvent
    public static void onKeyInput(int key, boolean pressed) {
    }

    @MultiLoaderEvent
    public static void onTagsUpdated() {
    }
}
