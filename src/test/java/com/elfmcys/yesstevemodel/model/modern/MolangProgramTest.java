package com.elfmcys.yesstevemodel.model.modern;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MolangProgramTest {
    static class Env extends MolangProgram.Environment {
        final Map<String, Double> values = new HashMap<>();
        final Map<String, MolangProgram> functions = new HashMap<>();
        final SecondOrderDynamics dynamics = new SecondOrderDynamics();
        double time;
        @Override public double read(String name) { return values.getOrDefault(name, 0d); }
        @Override public boolean isDefined(String name) { return values.containsKey(name); }
        @Override public void write(String name, double value) { values.put(name, value); }
        @Override public double function(String name, double[] args) { return functions.get(name).evaluate(this, args); }
        @Override public double secondOrder(String name, double input, double frequency, double damping, double response) { return dynamics.update(name, input, frequency, damping, response, time); }
    }
    static MolangProgram program(String text) { return MolangProgram.compile(text, new MolangParser()); }
    @Test void nestedFunctionsHaveArgumentsAndIsolatedTemporaryVariables() {
        Env env = new Env();
        env.functions.put("child", program("t.x=99; v.shared=v.shared+args[0]; return args[0]+args[1];"));
        assertEquals(12, program("t.x=7; t.y=fn.child(2,3); return t.x+t.y;").evaluate(env));
        assertEquals(2d, env.values.get("variable.shared"));
        assertEquals(0, program("return t.x+args[3];").evaluate(env));
    }
    @Test void blocksReturnImmediatelyAndUnselectedBranchesDoNotRun() {
        Env env = new Env();
        assertEquals(4, program("/* 说明 */ 1 ? {v.n=1; 0?{return 99;}:{return 4;};}: {v.n=2;}; v.n=3;").evaluate(env));
        assertEquals(1d, env.values.get("variable.n"));
        assertEquals(0, program("0 ? {return 8;};").evaluate(env));
        assertEquals(5, program("return 1?0?3:5:8;").evaluate(env));
    }
    @Test void loopsAndForEachPropagateBreakContinueAndReturn() {
        Env env = new Env();
        assertEquals(13, program("t.n=0;t.sum=0;loop(10,{t.n=t.n+1;t.n==2?{continue;};t.n==6?{break;};t.sum=t.sum+t.n;});return t.sum;").evaluate(env));
        assertEquals(9, program("t.sum=0;for_each(t.item,args,{t.sum=t.sum+t.item;});return t.sum;").evaluate(env, 2, 3, 4));
        assertEquals(3, program("for_each(t.item,args,{t.item==3?{return t.item;};});return -1;").evaluate(env, 2, 3, 4));
    }
    @Test void recursionBudgetRejectsCyclesAndResetsForLaterExecution() {
        Env env = new Env(); env.functions.put("cycle", program("return fn.cycle;"));
        assertThrows(IllegalStateException.class, () -> env.functions.get("cycle").evaluate(env));
        assertEquals(1, program("return 1;").evaluate(env));
        assertThrows(IllegalStateException.class, () -> program("loop(1024,{loop(1024,{v.n=v.n+1;});});").evaluate(env));
    }
    @Test void mathUsesDegreesAndCommentsPreserveQuotedPhysicsKeys() {
        Env env = new Env();
        assertEquals(6, program("//注释\n return math.sin(90)+math.clamp(8,0,5);").evaluate(env), 1e-8);
        assertEquals(8, program("return ysm.second_order('头发/*保持*/',8,2,0.6,0);").evaluate(env));
        assertThrows(IllegalArgumentException.class, () -> program("return ctrl.hold('one');"));
    }
    @Test void parserExpressionsUseActiveFunctionAndPhysicsLibrary() throws Exception {
        MolangParser parser = new MolangParser(); Map<String, MolangProgram> library = new HashMap<>();
        library.put("angle", program("return args[0]*2;"));
        try (MolangRuntime runtime = new MolangRuntime(library, new SecondOrderDynamics(), 1)) {
            assertEquals(12, parser.parseExpression("fn.angle(6)").get());
            assertEquals(4, parser.parseExpression("ysm.second_order('头发',4,1,0.4,0)").get());
        }
    }
    @Test void timelineCommentLabelsAndIndexedPositionDeltaAreSupported() throws Exception {
        MolangParser parser = new MolangParser(); parser.setValue("query.position_delta.0", () -> .125);
        assertEquals(0, parser.parseExpression("'玩家头发垂直角度物理'").get());
        assertEquals(12.5, parser.parseExpression("v.speed=100*q.position_delta(0); return v.speed;").get());
        assertEquals(0, parser.parseExpression("query.position_delta(3)").get());
    }
    @Test void originalPublicHairScriptCompilesAndChangesWithPitchAndVelocity() throws Exception {
        java.nio.file.Path path = Paths.get("examples/naytotime/functions/nt_hair.molang");
        assertTrue(Files.isRegularFile(path));
        MolangProgram hair = program(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
        Env env = new Env(); env.values.put("ysm.head_pitch", 0d);
        double initial = hair.evaluate(env, 0, 0, 0, 1, 1);
        assertTrue(Double.isFinite(initial));
        env.values.put("ysm.head_pitch", -60d); env.values.put("query.vertical_speed", -2d); env.values.put("ysm.input_vertical", 1d); env.values.put("ysm.ground_speed2", 3d);
        for (int i = 1; i <= 60; i++) { env.time = i / 60d; env.values.put("query.time_stamp", env.time * 20); hair.evaluate(env, 0, 0, 0, 1, 1); }
        double moved = hair.evaluate(env, 0, 0, 0, 1, 1);
        assertTrue(Math.abs(moved - initial) > 10);
        assertTrue(Double.isFinite(hair.evaluate(env, 1, -2, 1, 0.7, 0.5)));
    }

    @Test void nullCoalescingPreservesZeroAndOnlyEvaluatesTheNeededFallback() {
        Env env = new Env();
        assertEquals(7, program("v.missing ?? v.other ?? 7").evaluate(env));
        assertEquals(7, program("t.missing ?? 7").evaluate(env));
        assertEquals(0, program("v.zero=0; return v.zero ?? {v.side=1; return 7;};").evaluate(env));
        assertFalse(env.values.containsKey("variable.side"));
        assertEquals(0, program("t.zero=0; return t.zero ?? 7;").evaluate(env));
        assertEquals(3, program("1 ? (v.missing ?? 3) : 4").evaluate(env));
        assertEquals(5, program("(v.missing ?? 2) + 3").evaluate(env));
        assertEquals(0, program("0 ?? 7").evaluate(env));
    }

    @Test void defaultsStayMissingAcrossFramesAndDoNotLeakBetweenEntities() throws Exception {
        MolangParser parser = new MolangParser();
        com.elfmcys.yesstevemodel.geckolib3.core.molang.expressions.MolangExpression expression = parser.parseExpression("v.coalesce_fixture ?? 1");
        Map<String, Double> first = new HashMap<>(), second = new HashMap<>();
        try (MolangScope ignored = new MolangScope(first)) { assertEquals(1, expression.get()); }
        assertFalse(first.containsKey("variable.coalesce_fixture"));
        try (MolangScope ignored = new MolangScope(first)) {
            assertEquals(1, expression.get());
            parser.parseExpression("v.coalesce_fixture=0;").get();
            assertEquals(0, expression.get());
        }
        try (MolangScope ignored = new MolangScope(second)) { assertEquals(1, expression.get()); }
        try (MolangScope ignored = new MolangScope(first)) { assertEquals(0, expression.get()); }
        assertEquals(0d, first.get("variable.coalesce_fixture"));
        assertFalse(second.containsKey("variable.coalesce_fixture"));
    }

    @Test void nestedScopesRestoreThePresenceAndValueOfVariables() throws Exception {
        MolangParser parser = new MolangParser();
        com.elfmcys.yesstevemodel.geckolib3.core.molang.expressions.MolangExpression expression = parser.parseExpression("v.nested_fixture ?? 2");
        Map<String, Double> first = new HashMap<>(), second = new HashMap<>();
        first.put("variable.nested_fixture", 0d);
        try (MolangScope ignored = new MolangScope(first)) {
            assertEquals(0, expression.get());
            try (MolangScope nested = new MolangScope(second)) { assertEquals(2, expression.get()); }
            assertEquals(0, expression.get());
        }
        assertEquals(2, expression.get());
    }

    @Test void stringComparisonsKeepTheirCaseAndMissingIntegrationsUseEmptyStates() throws Exception {
        MolangParser parser = new MolangParser();
        assertEquals(1, parser.parseExpression("'bow' == 'bow'").get());
        assertEquals(0, parser.parseExpression("'Bow' == 'bow'").get());
        assertEquals(1, parser.parseExpression("'bow' != ''").get());
        assertEquals(0, parser.parseExpression("0 == ''").get());
        assertEquals(0, parser.parseExpression("(ctrl.carryon_type != '') || (ctrl.parcool_state != '') || (ctrl.swem_state != '') || (ctrl.slashblade_animation != '') || (ctrl.iss_animation != '')").get());
    }

    @Test void remiliaRandomIdleSelectionWorksInControllerEntryAndModernScripts() throws Exception {
        MolangParser parser = new MolangParser();
        for (String expression : new String[]{"v.idle_type = math.random_integer(1,5); return v.idle_type;", "v.idle_type = math.random_integer(1,5); return v.idle_type ?? 1;"}) {
            com.elfmcys.yesstevemodel.geckolib3.core.molang.expressions.MolangExpression compiled = parser.parseExpression(expression);
            for (int i = 0; i < 20; i++) { double value = compiled.get(); assertTrue(value >= 1 && value < 5); assertEquals(Math.floor(value), value); }
        }
        assertEquals(.5, parser.parseExpression("math.hermite_blend(0.5)").get(), 1e-10);
    }

    @Test void itemNameQueriesEvaluateAgainstCurrentContextAndSupportMultipleNames() throws Exception {
        MolangParser parser = new MolangParser();
        java.util.concurrent.atomic.AtomicBoolean matching = new java.util.concurrent.atomic.AtomicBoolean();
        parser.setStringQueryResolver(call -> matching.get() ? 1 : 0);
        com.elfmcys.yesstevemodel.geckolib3.core.molang.expressions.MolangExpression expression = parser.parseExpression("!query.is_item_name_any('mainhand','minecraft:diamond_sword','minecraft:iron_sword')");
        assertEquals(1, expression.get());
        matching.set(true); assertEquals(0, expression.get());
        assertEquals(0, parser.parseExpression("!q.is_item_name_any('offhand','minecraft:shield')").get());
        com.elfmcys.yesstevemodel.client.animation.modern.ControllerQueries queries = new com.elfmcys.yesstevemodel.client.animation.modern.ControllerQueries();
        assertEquals("!0", queries.resolve("!query.is_item_name_any('mainhand','minecraft:netherite_sword')", null, warning -> fail(warning)));
    }
}
