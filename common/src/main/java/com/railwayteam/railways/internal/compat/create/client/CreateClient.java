package com.railwayteam.railways.internal.compat.create.client;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.content.trains.GlobalRailwayManager;

public final class CreateClient {
    public static GlobalRailwayManager RAILWAYS() {
        // Create.RAILWAYS is the server manager, even in singleplayer. Client
        // packets must update the same separate manager used by bindCarriage().
        return AllClientHandle.INSTANCE.getGlobalRailwayManager();
    }

    private CreateClient() {
    }
}
