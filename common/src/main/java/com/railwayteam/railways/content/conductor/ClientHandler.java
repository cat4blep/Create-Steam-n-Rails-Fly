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

import org.jetbrains.annotations.Nullable;

public class ClientHandler {
	public static boolean isPlayerMountedOnCamera() {
		return false;
	}

	@Nullable
	public static ConductorEntity getPlayerMountedOnCamera() {
		return null;
	}

	public static boolean isPossessed(ConductorEntity conductor) {
		return false;
	}
}
