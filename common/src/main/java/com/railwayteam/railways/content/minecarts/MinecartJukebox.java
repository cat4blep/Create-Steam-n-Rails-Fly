package com.railwayteam.railways.content.minecarts;

import com.railwayteam.railways.registry.CREntities;
import com.railwayteam.railways.registry.CRItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

public class MinecartJukebox extends MinecartBlock {
    private ItemStack disc = ItemStack.EMPTY;

    public MinecartJukebox(EntityType<?> type, Level level) {
        super(type, level, Blocks.JUKEBOX);
    }

    public MinecartJukebox(Level level, double x, double y, double z) {
        super(CREntities.CART_JUKEBOX.get(), level, x, y, z, Blocks.JUKEBOX);
    }

    public int getComparatorOutput() {
        return 0;
    }

    public ItemStack getPickResult() {
        return CRItems.ITEM_JUKEBOXCART.asStack();
    }

    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        return super.interact(player, hand);
    }

    public void insertRecord(ItemStack record) {
        disc = record.copy();
    }

    public ItemStack getDisc() {
        return disc;
    }
}
