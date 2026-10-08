package com.elfmcys.yesstevemodel.model.modern;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SecondOrderDynamicsTest {
    @Test void repeatedBonesDoNotAdvanceTheSameFrameAndPlayersHaveSeparateState() {
        SecondOrderDynamics first = new SecondOrderDynamics(), second = new SecondOrderDynamics();
        first.update("hair", 0, 2, .6, 0, 0);
        double output = first.update("hair", 60, 2, .6, 0, .05);
        for (int i = 0; i < 138; i++) assertEquals(output, first.update("hair", 60, 2, .6, 0, .05));
        assertEquals(-60, second.update("hair", -60, 2, .6, 0, .05));
        assertTrue(output < 60);
    }
    @Test void stepResponseConvergesAtLowAndHighFrameRates() {
        for (int fps : new int[]{5, 20, 60, 120}) {
            SecondOrderDynamics dynamics = new SecondOrderDynamics(); dynamics.update("hair", 0, 2, .6, 0, 0);
            double output = 0;
            for (int frame = 1; frame <= fps * 4; frame++) { output = dynamics.update("hair", 60, 2, .6, 0, frame / (double) fps); assertTrue(Double.isFinite(output)); assertTrue(Math.abs(output) < 120); }
            assertEquals(60, output, .05);
        }
    }
    @Test void modelReloadClockRewindAndLongPauseResetVelocity() {
        SecondOrderDynamics dynamics = new SecondOrderDynamics(); dynamics.update("hair", 0, 2, .6, 0, 1);
        dynamics.update("hair", 30, 2, .6, 0, 1.1);
        assertEquals(-10, dynamics.update("hair", -10, 2, .6, 0, .5));
        assertEquals(70, dynamics.update("hair", 70, 2, .6, 0, 2));
        dynamics.clear(); assertEquals(5, dynamics.update("hair", 5, 2, .6, 0, 2.1));
        assertEquals(0, dynamics.update("hair", Double.NaN, 2, .6, 0, 2.2));
    }
}
