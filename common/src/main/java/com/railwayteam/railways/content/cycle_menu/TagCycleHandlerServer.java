package com.railwayteam.railways.content.cycle_menu;

import com.railwayteam.railways.annotation.multiloader.MultiLoaderEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

public class TagCycleHandlerServer {
    public static final TagCycleTracker CYCLE_TRACKER = new TagCycleTracker();

    public static void select(ServerPlayer player, Item target) {
    }

    @MultiLoaderEvent
    public static void onTagsUpdated() {
    }
}
