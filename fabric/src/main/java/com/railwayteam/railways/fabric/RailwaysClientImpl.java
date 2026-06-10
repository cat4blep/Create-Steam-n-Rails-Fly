/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
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

package com.railwayteam.railways.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.RailwaysClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class RailwaysClientImpl implements ClientModInitializer {
	public void onInitializeClient() {
		RailwaysClient.init();
	}

	@SuppressWarnings({"unchecked", "rawtypes"}) // jank!
	public static void registerClientCommands(Consumer<CommandDispatcher<SharedSuggestionProvider>> consumer) {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			CommandDispatcher<SharedSuggestionProvider> casted = (CommandDispatcher) dispatcher;
			consumer.accept(casted);
		});
	}

	public static void registerModelLayer(ModelLayerLocation layer, Supplier<LayerDefinition> definition) {
		EntityModelLayerRegistry.registerModelLayer(layer, definition::get);
	}

	public static <T extends Entity> void registerEntityRenderer(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
		EntityRenderers.register(type, provider);
	}

	public static void registerBuiltinPack(String id, String name) {
		ModContainer mod = FabricLoader.getInstance().getModContainer(Railways.MOD_ID).orElseThrow();
		ResourceManagerHelper.registerBuiltinResourcePack(Railways.asResource(id), mod, Component.literal(name), ResourcePackActivationType.NORMAL);
	}
}
