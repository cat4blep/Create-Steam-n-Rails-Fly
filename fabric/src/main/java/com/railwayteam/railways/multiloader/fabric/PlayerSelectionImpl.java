package com.railwayteam.railways.multiloader.fabric;

import com.railwayteam.railways.multiloader.PlayerSelection;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Predicate;

public class PlayerSelectionImpl {
    private static final PlayerSelection NONE = new PlayerSelection() {
        @Override
        public void accept(Identifier id, FriendlyByteBuf buffer) {
        }
    };

    public static PlayerSelection all() { return NONE; }
    public static PlayerSelection allWith(Predicate<ServerPlayer> condition) { return NONE; }
    public static PlayerSelection of(ServerPlayer player) { return NONE; }
    public static PlayerSelection tracking(Entity entity) { return NONE; }
    public static PlayerSelection trackingWith(Entity entity, Predicate<ServerPlayer> condition) { return NONE; }
    public static PlayerSelection tracking(BlockEntity be) { return NONE; }
    public static PlayerSelection tracking(ServerLevel level, BlockPos pos) { return NONE; }
    public static PlayerSelection trackingAndSelf(ServerPlayer player) { return NONE; }
}
