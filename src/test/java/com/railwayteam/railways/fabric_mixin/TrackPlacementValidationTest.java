package com.railwayteam.railways.fabric_mixin;

import com.zurrtum.create.content.trains.track.TrackPlacement;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrackPlacementValidationTest {
    @Test
    void failedSimulationReturnsBeforeInventoryOrPavementAccess() throws Exception {
        Method callback = callback();
        Inject injection = callback.getAnnotation(Inject.class);
        assertNotNull(injection);
        assertTrue(injection.cancellable());
        assertArrayEquals(new String[]{"tryConnect"}, injection.method());
        At point = injection.at()[0];
        assertEquals("INVOKE", point.value());
        assertEquals(0, point.ordinal());
        assertEquals(At.Shift.AFTER, point.shift());

        try (InputStream bytes = getClass().getClassLoader().getResourceAsStream(
            "com/zurrtum/create/content/trains/track/TrackPlacement.class"
        )) {
            assertNotNull(bytes);
            List<String> calls = ClassFile.of().parse(bytes.readAllBytes()).methods().stream()
                .filter(method -> method.methodName().stringValue().equals("tryConnect"))
                .findFirst().orElseThrow().code().orElseThrow().elementStream()
                .filter(InvokeInstruction.class::isInstance).map(InvokeInstruction.class::cast)
                .map(call -> "L" + call.method().owner().asInternalName() + ";"
                    + call.method().name().stringValue() + call.method().type().stringValue())
                .toList();
            int simulation = calls.indexOf(point.target());
            int offhand = indexOfCall(calls, ";getOffhandItem(");
            int pavement = indexOfCall(calls, ";paveTracks(");
            int inventory = indexOfCall(calls, ";getInventory(");
            assertTrue(simulation >= 0, "The resolved Fly API must still call the injected simulation target");
            assertTrue(simulation < offhand && offhand < pavement && pavement < inventory,
                "The cancellation point must run before paving checks and inventory mutation");
            assertNotEquals(simulation, calls.lastIndexOf(point.target()), "Actual placement must follow simulation");
        }

        TrackPlacement.PlacementInfo info = new TrackPlacement.PlacementInfo(null);
        info.valid = false;
        CallbackInfoReturnable<TrackPlacement.PlacementInfo> result = new CallbackInfoReturnable<>("tryConnect", true);
        callback.invoke(null, result, info);
        assertTrue(result.isCancelled());
        assertSame(info, result.getReturnValue());
    }

    @Test
    void successfulSimulationContinuesToNormalResourceChecks() throws Exception {
        TrackPlacement.PlacementInfo info = new TrackPlacement.PlacementInfo(null);
        info.valid = true;
        CallbackInfoReturnable<TrackPlacement.PlacementInfo> result = new CallbackInfoReturnable<>("tryConnect", true);
        callback().invoke(null, result, info);
        assertFalse(result.isCancelled());
    }

    private static Method callback() throws NoSuchMethodException {
        Method callback = MixinTrackPlacement.class.getDeclaredMethod("railways$stopInvalidPlacement",
            CallbackInfoReturnable.class, TrackPlacement.PlacementInfo.class);
        callback.setAccessible(true);
        return callback;
    }

    private static int indexOfCall(List<String> calls, String signature) {
        for (int i = 0; i < calls.size(); i++) {
            if (calls.get(i).contains(signature))
                return i;
        }
        fail("Missing Create Fly call " + signature);
        return -1;
    }
}
