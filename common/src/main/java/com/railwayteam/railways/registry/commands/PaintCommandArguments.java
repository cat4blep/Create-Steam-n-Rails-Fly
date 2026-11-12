/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.registry.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.railwayteam.railways.content.palettes.PalettesColor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

final class PaintCommandArguments {
    private static final List<String> COLORS = Arrays.stream(PalettesColor.values())
        .map(PalettesColor::getSerializedName).toList();
    private static final DynamicCommandExceptionType INVALID_COLOR =
        new DynamicCommandExceptionType(color -> Component.literal("Unknown paint color: " + color));

    private PaintCommandArguments() {
    }

    static RequiredArgumentBuilder<CommandSourceStack, String> colorArgument(String name) {
        return Commands.argument(name, StringArgumentType.word())
            .suggests((context, builder) -> SharedSuggestionProvider.suggest(COLORS, builder));
    }

    static PalettesColor getColor(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        String value = StringArgumentType.getString(context, name);
        PalettesColor color = PalettesColor.byName(value, null);
        if (color == null) {
            throw INVALID_COLOR.create(value);
        }
        return color;
    }
}
