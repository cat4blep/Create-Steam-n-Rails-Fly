/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.conductor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class ConductorPossessionController {
	@Environment(EnvType.CLIENT)
	private static ClientChunkCache.Storage cameraStorage;

	@Environment(EnvType.CLIENT)
	public static void onClientTick(Minecraft mc, boolean start) {
	}

	@Environment(EnvType.CLIENT)
	public static void onHandleKeybinds(Minecraft mc, boolean start) {
	}

	@Environment(EnvType.CLIENT)
	public static void dismount() {
	}

	@Environment(EnvType.CLIENT)
	public static ClientChunkCache.Storage getCameraStorage() {
		return cameraStorage;
	}

	@Environment(EnvType.CLIENT)
	public static void setCameraStorage(ClientChunkCache.Storage newStorage) {
		cameraStorage = newStorage;
	}

	@Environment(EnvType.CLIENT)
	public static void setRenderPosition(Entity entity) {
	}

	@Environment(EnvType.CLIENT)
	public static void tryUpdatePossession(ConductorEntity conductorEntity) {
	}

	public static boolean isPossessingConductor(Entity entity) {
		return false;
	}

	@Nullable
	public static ConductorEntity getPossessingConductor(Entity entity) {
		return null;
	}

	@Environment(EnvType.CLIENT)
	public static boolean wasUpPressed() {
		return false;
	}

	@Environment(EnvType.CLIENT)
	public static boolean wasDownPressed() {
		return false;
	}

	@Environment(EnvType.CLIENT)
	public static boolean wasLeftPressed() {
		return false;
	}

	@Environment(EnvType.CLIENT)
	public static boolean wasRightPressed() {
		return false;
	}

	@Environment(EnvType.CLIENT)
	public static boolean wasSprintPressed() {
		return false;
	}

	@Environment(EnvType.CLIENT)
	public static boolean wasJumpPressed() {
		return false;
	}
}
