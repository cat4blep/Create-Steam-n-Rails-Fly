package com.railwayteam.railways.content.conductor;

import com.railwayteam.railways.Railways;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public class ConductorRenderState extends LivingEntityRenderState {
    public DyeColor color = ConductorEntity.defaultColor();
    public ItemStack headStack = ItemStack.EMPTY;
    public final ItemStackRenderState secondaryHeadRenderState = new ItemStackRenderState();
    public Identifier texture = Railways.asResource("textures/entity/conductor.png");
}
