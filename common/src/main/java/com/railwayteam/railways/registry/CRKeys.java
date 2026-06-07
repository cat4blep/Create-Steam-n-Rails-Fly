package com.railwayteam.railways.registry;

import net.minecraft.client.KeyMapping;

import java.util.HashSet;
import java.util.Set;

public enum CRKeys {
    BOGEY_MENU,
    CYCLE_MENU;

    public static final Set<KeyMapping> NON_CONFLICTING_KEYMAPPINGS = new HashSet<>();

    public static void register() {
    }

    public static void fixBinds() {
    }

    public KeyMapping getKeybind() {
        return null;
    }

    public boolean isPressed() {
        return false;
    }

    public String getBoundKey() {
        return "";
    }

    public int getBoundCode() {
        return 0;
    }

    public static boolean isKeyDown(int key) {
        return false;
    }

    public static boolean isMouseButtonDown(int button) {
        return false;
    }

    public static boolean ctrlDown() {
        return false;
    }

    public static boolean shiftDown() {
        return false;
    }

    public static boolean altDown() {
        return false;
    }
}
