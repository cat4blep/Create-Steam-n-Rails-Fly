/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.internal.compat;

import com.railwayteam.railways.internal.compat.create.client.CreateClient;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.trains.GlobalRailwayManager;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.metadata.BuiltinModMetadata;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CreateClientRailwaysTest {
    private GlobalRailwayManager previousServer;
    private AllClientHandle previousClient;
    private GlobalRailwayManager server;
    private GlobalRailwayManager client;

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        // Create's static initializer reads its Fabric version. Supply only that
        // metadata in JUnit, without running registration or starting a game.
        var field = FabricLoaderImpl.class.getDeclaredField("modMap");
        field.setAccessible(true);
        var mods = (Map<String, ModContainer>) field.get(FabricLoaderImpl.INSTANCE);
        var metadata = new BuiltinModMetadata.Builder("create", "6.0.9-1").build();
        var container = (ModContainer) Proxy.newProxyInstance(ModContainer.class.getClassLoader(),
            new Class<?>[] {ModContainer.class}, (proxy, method, args) -> {
                if (method.getName().equals("getMetadata")) return metadata;
                throw new UnsupportedOperationException(method.getName());
            });
        ModContainer previous = mods.putIfAbsent("create", container);
        try {
            Class.forName(Create.class.getName());
        } finally {
            if (previous == null) mods.remove("create", container);
        }
    }

    @BeforeEach
    void separateSides() {
        previousServer = Create.RAILWAYS;
        previousClient = AllClientHandle.INSTANCE;
        server = new GlobalRailwayManager();
        client = new GlobalRailwayManager();
        Create.RAILWAYS = server;
        AllClientHandle.INSTANCE = clientHandle(client);
    }

    @AfterEach
    void restoreSides() {
        Create.RAILWAYS = previousServer;
        AllClientHandle.INSTANCE = previousClient;
    }

    @Test
    void couplingCanStillFindRearClientTrainAfterServerHasMergedIt() {
        UUID frontId = UUID.randomUUID();
        UUID rearId = UUID.randomUUID();
        // combineTrains removes the rear train from the server before sending
        // AddTrainEndPacket, which still needs both original CLIENT trains.
        // Only key ownership is needed here. Null placeholders avoid loading
        // carriage entities, which require Fabric access widening outside JUnit.
        server.trains.put(frontId, null);
        client.trains.put(frontId, null);
        client.trains.put(rearId, null);

        assertTrue(CreateClient.RAILWAYS().trains.containsKey(rearId));
        assertSame(client.trains, CreateClient.RAILWAYS().trains);
        assertTrue(server.trains.containsKey(frontId));
        assertFalse(server.trains.containsKey(rearId));
    }

    @Test
    void clientMutationDoesNotChangeTheIntegratedServerTrainMap() {
        UUID id = UUID.randomUUID();
        server.trains.put(id, null);
        client.trains.put(id, null);

        CreateClient.RAILWAYS().trains.remove(id);
        assertTrue(server.trains.containsKey(id));
        assertFalse(client.trains.containsKey(id));
    }

    @Test
    void reconnectUsesTheCurrentClientManagerEvenWithoutAnIntegratedServer() {
        Create.RAILWAYS = null;
        assertSame(client, CreateClient.RAILWAYS());
        var nextClient = new GlobalRailwayManager();
        AllClientHandle.INSTANCE = clientHandle(nextClient);
        assertSame(nextClient, CreateClient.RAILWAYS());
    }

    private static AllClientHandle clientHandle(GlobalRailwayManager manager) {
        return new AllClientHandle() {
            @Override
            public GlobalRailwayManager getGlobalRailwayManager() {
                return manager;
            }
        };
    }
}
