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
import com.railwayteam.railways.base.datafix.CRReferences;
import com.railwayteam.railways.base.datafix.fixes.UpsideDownMonoBogeyFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class V0Test {
    private static com.mojang.datafixers.DataFixer fixer(boolean trinkets) {
        DataFixerBuilder builder = new DataFixerBuilder(1);
        Schema parent = trinkets ? new TrinketsSchema(100, null) : new VanillaSchema(100, null);
        builder.addSchema(0, (version, ignored) -> new V0(version, parent));
        Schema v1 = builder.addSchema(1, NamespacedSchema::new);
        builder.addFixer(new UpsideDownMonoBogeyFix(v1, "legacy bogey"));
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
