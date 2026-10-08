package com.elfmcys.yesstevemodel.model.modern;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.google.gson.JsonObject;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BlendTransitionTest {
    private static JsonObject json(String text) { return YesSteveModel.GSON.fromJson(text.replace('\'', '"'), JsonObject.class); }
    private static ControllerMachine machine(String blend) throws IOException {
        return new ControllerMachine(ControllerDefinition.parse(json("{'states':{'default':{'transitions':[{'next':'q.state_time >= 1'}],'blend_transition':" + blend + "},'next':{}}}")));
    }
    private static ControllerMachine start(String blend) throws IOException {
        ControllerMachine machine = machine(blend);
        ModernRuntimeTest.Evaluation evaluation = new ModernRuntimeTest.Evaluation();
        machine.update(100, evaluation);
        machine.update(120, evaluation);
        assertEquals("next", machine.state());
        return machine;
    }

    @Test void remiliaCurveUsesSecondsAndOutgoingWeightsAtEveryPointAndBetweenThem() throws Exception {
        ControllerMachine machine = start("{'0.0':1,'0.0333':0.96571,'0.0667':0.8738,'0.1':0.74074,'0.1333':0.58299,'0.1667':0.41701,'0.2':0.25926,'0.2333':0.1262,'0.2667':0.03429,'0.3':0}");
        double[] times = {0, .0333, .0667, .1, .1333, .1667, .2, .2333, .2667, .3};
        double[] outgoing = {1, .96571, .8738, .74074, .58299, .41701, .25926, .1262, .03429, 0};
        for (int i = 0; i < times.length; i++) {
            assertEquals(1 - outgoing[i], machine.blend(120 + times[i] * 20), 1e-10);
            if (i > 0) assertEquals(1 - (outgoing[i - 1] + outgoing[i]) / 2, machine.blend(120 + (times[i - 1] + times[i]) * 10), 1e-10);
        }
        assertEquals(1, machine.blend(1000));
        assertNotEquals(.1 / .3, machine.blend(122), 1e-3);
    }

    @Test void missingZeroDefaultsToFullOutgoingWeightAndKeysAreSortedNumerically() throws Exception {
        ControllerMachine machine = start("{'0.4':0,'0.1':0.8}");
        assertEquals(0, machine.blend(120));
        assertEquals(.1, machine.blend(121), 1e-10);
        assertEquals(.6, machine.blend(125), 1e-10);
        assertEquals(1, machine.blend(128));
    }

    @Test void explicitZeroAndLastWeightsArePreservedIncludingOvershoot() throws Exception {
        ControllerMachine machine = start("{'0':0.25,'0.1':-0.2,'0.2':0.1}");
        assertEquals(.75, machine.blend(120));
        assertEquals(1.2, machine.blend(122), 1e-10);
        assertEquals(.9, machine.blend(1000), 1e-10);
    }

    @Test void scalarMolangDurationIsEvaluatedOnceInTheDepartingState() throws Exception {
        ControllerMachine machine = start("'q.state_time * 0.5'");
        assertEquals(0, machine.blend(120));
        assertEquals(.5, machine.blend(125));
        assertEquals(1, machine.blend(130));
    }

    @Test void emptyZeroNegativeAndNonFiniteDurationsDoNotLeakInvalidPoseWeights() throws Exception {
        for (String blend : new String[]{"{}", "0", "-1"}) assertEquals(1, start(blend).blend(120));
        for (double invalid : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            ControllerMachine machine = machine("'v.duration'");
            ModernRuntimeTest.Evaluation evaluation = new ModernRuntimeTest.Evaluation();
            evaluation.variable("variable.duration", invalid);
            machine.update(100, evaluation); machine.update(120, evaluation);
            assertEquals(1, machine.blend(120));
        }
    }

    @Test void malformedCurveFailsAtLoadWithStateNameInsteadOfDuringRendering() {
        for (String blend : new String[]{"[]", "true", "null", "{'bad':0}", "{'-0.1':0}", "{'NaN':0}", "{'Infinity':0}", "{'0':{}}", "{'0':'v.weight'}", "{'0':null}", "{'0':1,'0.0':0}"}) {
            IOException error = assertThrows(IOException.class, () -> machine(blend), blend);
            assertTrue(error.getMessage().contains("default"), error.getMessage());
        }
    }

    @Test void nextTransitionReplacesThePreviousCurveAndMachinesStayIndependent() throws Exception {
        ControllerDefinition definition = ControllerDefinition.parse(json("{'states':{'default':{'transitions':[{'next':'q.state_time >= 1'}],'blend_transition':{'0':1,'0.3':0}},'next':{'transitions':[{'last':'q.state_time >= 1'}],'blend_transition':0.5},'last':{}}}"));
        ControllerMachine first = new ControllerMachine(definition), second = new ControllerMachine(definition);
        ModernRuntimeTest.Evaluation evaluation = new ModernRuntimeTest.Evaluation();
        first.update(100, evaluation); second.update(110, evaluation);
        first.update(120, evaluation); second.update(120, evaluation);
        assertEquals(1, second.blend(123));
        assertEquals(.5, first.blend(123), 1e-10);
        first.update(140, evaluation);
        assertEquals(.5, first.blend(145));
        assertEquals("default", second.state());
    }
}
