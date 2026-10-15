/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.conductor.toolbox;

import org.junit.jupiter.api.Test;

import java.lang.classfile.ClassFile;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.TypeCheckInstruction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MountedToolboxApiTest {
    @Test
    void toolboxOperationsDoNotCastRegisteredToolboxesToValveHandles() throws Exception {
        // Create Fly's TOOLBOX field is declared ColorCollection<ValveHandleBlock>,
        // despite registering ToolboxBlock. Java otherwise inserts a crashing
        // cast before even calling Block.defaultBlockState()/asItem().
        String resource = "com/railwayteam/railways/content/conductor/toolbox/MountedToolbox.class";
        try (var stream = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(stream);
            var toolboxClass = ClassFile.of().parse(stream.readAllBytes());
            for (var method : toolboxClass.methods()) {
                if (method.code().isEmpty())
                    continue;
                boolean badCast = method.code().get().elementStream()
                    .filter(TypeCheckInstruction.class::isInstance)
                    .map(TypeCheckInstruction.class::cast)
                    .anyMatch(instruction -> instruction.opcode() == Opcode.CHECKCAST
                        && instruction.type().asInternalName().equals(
                            "com/zurrtum/create/content/kinetics/crank/ValveHandleBlock"));
                assertFalse(badCast, () -> method.methodName() + " casts ToolboxBlock to ValveHandleBlock");
            }
        }
    }
}
