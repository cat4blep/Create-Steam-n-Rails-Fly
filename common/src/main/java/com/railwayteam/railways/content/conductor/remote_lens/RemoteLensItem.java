/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.conductor.remote_lens;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RemoteLensItem extends Item {
	public RemoteLensItem(Properties properties) {
		super(properties);
	}

	public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
		tooltip.add(Component.translatable("railways.remote_lens.tool.not_bound").withStyle(ChatFormatting.DARK_RED));
	}

	public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player,
														   @NotNull LivingEntity target, @NotNull InteractionHand hand) {
		return InteractionResult.PASS;
	}

	public @NotNull InteractionResult useOn(UseOnContext context) {
		return InteractionResult.PASS;
	}

	public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
		return InteractionResult.PASS;
	}
}
