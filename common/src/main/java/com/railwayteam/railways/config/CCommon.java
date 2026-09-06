/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
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

package com.railwayteam.railways.config;

import com.zurrtum.create.catnip.config.ConfigBase;
import com.zurrtum.create.catnip.config.ui.ConfigAnnotations;

@SuppressWarnings("unused")
public class CCommon extends ConfigBase {

    static final boolean DEFAULT_REGISTER_MISSING_TRACKS = true;

    public final ConfigBool registerMissingTracks = b(DEFAULT_REGISTER_MISSING_TRACKS, "registerMissingTracks", Comments.registerMissingTracks, ConfigAnnotations.RequiresRestart.BOTH.asComment());
    public final ConfigBool disableDatafixer = b(false, "disableDatafixer", Comments.disableDatafixer, ConfigAnnotations.RequiresRestart.BOTH.asComment());
    public String getName() {
        return "common";
    }

    private static class Comments {
        static String registerMissingTracks = "Register integration tracks for mods that are not present. Keep this setting the same on the server and clients; changing it requires a full restart.";
        static String disableDatafixer = "Disable Steam 'n' Rails datafixers. Do not enable this config if your world contains pre-Create 0.5.1 monobogeys, because then they will be destroyed";
    }
}
