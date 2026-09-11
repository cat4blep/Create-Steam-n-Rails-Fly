/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.base.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.railwayteam.railways.base.datafix.CRReferences;
import com.railwayteam.railways.base.datafix.fixes.UpsideDownMonoBogeyFix;
import com.railwayteam.railways.base.datafix.fixes.StreamlinedSmokeStackFacingFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class V0Test {
    private static com.mojang.datafixers.DataFixer fixer(boolean trinkets) {
        DataFixerBuilder builder = new DataFixerBuilder(2);
        Schema parent = trinkets ? new TrinketsSchema(100, null) : new VanillaSchema(100, null);
        builder.addSchema(0, (version, ignored) -> new V0(version, parent));
        Schema v1 = builder.addSchema(1, NamespacedSchema::new);
        builder.addFixer(new UpsideDownMonoBogeyFix(v1, "legacy bogey"));
        Schema v2 = builder.addSchema(2, NamespacedSchema::new);
        builder.addFixer(new StreamlinedSmokeStackFacingFix(v2, "legacy streamlined smokestack"));
        return builder.build().fixer();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void migratesCarriagePaletteWithWrappedEntitySchema(boolean trinkets) {
        var input = JsonParser.parseString("""
            {"data":{"Trains":[{"Carriages":[{"Entity":{
              "id":"create:carriage_contraption",
              "Contraption":{"Blocks":{"Palette":[{"Name":"railways:mono_bogey_upside_down"}]}},
              "trinkets":{"hat":{"Items":[{"id":"minecraft:diamond"}]}},
              "OtherModData":{"value":42}
            }}]}]}}
            """);
        var output = fixer(trinkets).update(CRReferences.SAVED_DATA_CREATE_TRACKS,
            new Dynamic<>(JsonOps.INSTANCE, input), 0, 1).getValue();
        var entity = output.getAsJsonObject().getAsJsonObject("data").getAsJsonArray("Trains")
            .get(0).getAsJsonObject().getAsJsonArray("Carriages").get(0).getAsJsonObject()
            .getAsJsonObject("Entity");
        assertEquals("railways:mono_bogey", entity.getAsJsonObject("Contraption")
            .getAsJsonObject("Blocks").getAsJsonArray("Palette").get(0).getAsJsonObject()
            .get("Name").getAsString());
        assertEquals("true", entity.getAsJsonObject("Contraption").getAsJsonObject("Blocks")
            .getAsJsonArray("Palette").get(0).getAsJsonObject().getAsJsonObject("Properties")
            .get("upside_down").getAsString());
        assertEquals("minecraft:diamond", entity.getAsJsonObject("trinkets").getAsJsonObject("hat")
            .getAsJsonArray("Items").get(0).getAsJsonObject().get("id").getAsString());
        assertEquals(42, entity.getAsJsonObject("OtherModData").get("value").getAsInt());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void preservesInheritedChoicesAndMigratesStandaloneEntities(boolean trinkets) {
        var input = JsonParser.parseString("""
            {"id":"test:carrier","BlockState":{"Name":"railways:mono_bogey_upside_down"},
             "cardinal_components":{"trinkets:trinkets":{"hat":{"Items":[{"id":"minecraft:diamond"}]}}},
             "equipment":{"head":{"id":"minecraft:iron_helmet"}},"CustomName":"Carrier",
             "OtherModData":{"value":42}}
            """);
        var fixer = fixer(trinkets);
        var fixed = fixer.update(References.ENTITY, new Dynamic<>(JsonOps.INSTANCE, input), 0, 1).getValue();
        var expected = input.deepCopy().getAsJsonObject();
        expected.add("BlockState", JsonParser.parseString("""
            {"Name":"railways:mono_bogey","Properties":{"upside_down":"true"}}
            """));
        assertEquals(expected, fixed, "only the legacy block state should change");
        assertEquals(fixed, fixer.update(References.ENTITY, new Dynamic<>(JsonOps.INSTANCE, fixed), 0, 1).getValue());
    }

    @ParameterizedTest
    @CsvSource({"false,chunk", "true,chunk", "false,player", "true,player"})
    void migratesRootVehicleAndPassengersWithoutLosingOtherModData(boolean trinkets, String rootType) {
        JsonObject entity = JsonParser.parseString("""
            {"id":"create:carriage_contraption",
             "Contraption":{"Blocks":{"Palette":[{"Name":"railways:mono_bogey_upside_down"}]}},
             "Passengers":[{"id":"test:carrier","BlockState":{"Name":"railways:mono_bogey_upside_down"},
               "trinkets":{"hat":{"Items":[{"id":"minecraft:diamond"}]}}}],
             "cardinal_components":{"trinkets:trinkets":{"hat":{"Items":[{"id":"minecraft:iron_helmet"}]}}},
             "OtherModData":{"value":42}}
            """).getAsJsonObject();
        JsonObject expectedEntity = entity.deepCopy();
        var fixedBlock = JsonParser.parseString("""
            {"Name":"railways:mono_bogey","Properties":{"upside_down":"true"}}
            """);
        expectedEntity.getAsJsonObject("Contraption").getAsJsonObject("Blocks")
            .getAsJsonArray("Palette").set(0, fixedBlock.deepCopy());
        expectedEntity.getAsJsonArray("Passengers").get(0).getAsJsonObject()
            .add("BlockState", fixedBlock.deepCopy());

        JsonObject input = wrapEntity(entity, rootType);
        JsonObject expected = wrapEntity(expectedEntity, rootType);
        var type = rootType.equals("chunk") ? References.ENTITY_CHUNK : References.PLAYER;
        var fixer = fixer(trinkets);
        var output = fixer.update(type, new Dynamic<>(JsonOps.INSTANCE, input), 0, 1).getValue();
        assertEquals(expected, output, "both palettes migrate while unrelated fields survive");
        assertEquals(output, fixer.update(type, new Dynamic<>(JsonOps.INSTANCE, output), 0, 1).getValue());
    }

    private static JsonObject wrapEntity(JsonObject entity, String rootType) {
        JsonObject root = new JsonObject();
        if (rootType.equals("chunk")) {
            var entities = new com.google.gson.JsonArray();
            entities.add(entity);
            root.add("Entities", entities);
        } else {
            JsonObject vehicle = new JsonObject();
            vehicle.add("Entity", entity);
            vehicle.addProperty("Attach", "00000000-0000-0000-0000-000000000001");
            root.add("RootVehicle", vehicle);
            root.addProperty("XpLevel", 25);
        }
        root.addProperty("OtherRootData", "preserved");
        return root;
    }

    @ParameterizedTest
    @CsvSource({"x,east", "z,north"})
    void migratesStreamlinedSmokestackAxis(String axis, String facing) {
        var input = JsonParser.parseString("""
            {"Name":"railways:smokestack_streamlined","Properties":{"axis":"%s","waterlogged":"false"}}
            """.formatted(axis));
        var expected = JsonParser.parseString("""
            {"Name":"railways:smokestack_streamlined","Properties":{"facing":"%s","waterlogged":"false"}}
            """.formatted(facing));
        assertEquals(expected, fixer(false).update(References.BLOCK_STATE,
            new Dynamic<>(JsonOps.INSTANCE, input), 1, 2).getValue());
    }

    private static class VanillaSchema extends NamespacedSchema {
        VanillaSchema(int version, Schema parent) { super(version, parent); }

        @Override
        public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
            Map<String, Supplier<TypeTemplate>> entities = new HashMap<>();
            schema.register(entities, "test:carrier",
                () -> DSL.optionalFields("BlockState", References.BLOCK_STATE.in(schema)));
            return entities;
        }

        @Override
        public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) { return new HashMap<>(); }

        @Override
        public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entities,
                                  Map<String, Supplier<TypeTemplate>> blockEntities) {
            schema.registerType(false, References.BLOCK_STATE, DSL::remainder);
            schema.registerType(false, References.ITEM_STACK, DSL::remainder);
            schema.registerType(false, References.ENTITY_EQUIPMENT,
                () -> DSL.optional(DSL.field("equipment", DSL.remainder())));
            schema.registerType(false, References.TEXT_COMPONENT, () -> DSL.constType(DSL.string()));
            schema.registerType(true, References.ENTITY,
                () -> DSL.taggedChoiceLazy("id", namespacedString(), entities));
            schema.registerType(true, References.ENTITY_TREE,
                () -> DSL.optionalFields("Passengers", DSL.list(References.ENTITY_TREE.in(schema)),
                    References.ENTITY.in(schema)));
            schema.registerType(false, References.ENTITY_CHUNK,
                () -> DSL.optionalFields("Entities", DSL.list(References.ENTITY_TREE.in(schema))));
            schema.registerType(false, References.PLAYER,
                () -> DSL.optionalFields("RootVehicle", DSL.optionalFields("Entity", References.ENTITY_TREE.in(schema))));
        }
    }

    private static class TrinketsSchema extends VanillaSchema {
        TrinketsSchema(int version, Schema parent) { super(version, parent); }

        @Override
        public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entities,
                                  Map<String, Supplier<TypeTemplate>> blockEntities) {
            super.registerTypes(schema, entities, blockEntities);
            schema.registerType(true, References.ENTITY, () -> {
                var trinketData = DSL.optional(DSL.compoundList(DSL.and(
                    DSL.optional(DSL.compoundList(DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema))))),
                    DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema))),
                    DSL.optionalFields("cosmetic", DSL.list(References.ITEM_STACK.in(schema))))));
                // Shape injected by Trinkets Updated's V1460Mixin.
                return DSL.allWithRemainder(
                    DSL.optional(DSL.field("cardinal_components", DSL.optionalFields("trinkets:trinkets", trinketData))),
                    DSL.optional(DSL.optionalFields("trinkets", trinketData)),
                    DSL.taggedChoiceLazy("id", namespacedString(), entities));
            });
        }
    }
}
