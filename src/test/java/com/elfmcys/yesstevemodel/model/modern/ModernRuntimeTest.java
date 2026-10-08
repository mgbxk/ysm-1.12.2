package com.elfmcys.yesstevemodel.model.modern;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.animation.modern.ModelSoundPack;
import com.elfmcys.yesstevemodel.client.animation.modern.ModernAssets;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatableModel;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.AnimationProcessor;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone;
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationFile;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser;
import com.elfmcys.yesstevemodel.geckolib3.util.json.JsonAnimationUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ModernRuntimeTest {
    private static JsonObject json(String text) { return YesSteveModel.GSON.fromJson(text.replace('\'', '"'), JsonObject.class); }
    private static Animation animation(String text, MolangParser parser) throws Exception {
        JsonObject file = new JsonObject(); file.add("animations", json(text));
        return JsonAnimationUtils.deserializeJsonToAnimation(JsonAnimationUtils.getAnimation(file, "a"), parser);
    }

    static class Evaluation implements ControllerMachine.Evaluation {
        final MolangParser parser = new MolangParser();
        final List<String> events = new ArrayList<>();
        @Override public double value(String expression) { try { return parser.parseExpression(expression).get(); } catch (Exception e) { throw new AssertionError(e); } }
        @Override public void variable(String name, double value) { parser.setValue(name, () -> value); }
        @Override public void scripts(JsonElement scripts) {
            if (scripts != null) for (JsonElement script : scripts.getAsJsonArray()) { events.add(script.getAsString()); value(script.getAsString()); }
        }
        @Override public void enterSounds(JsonElement sounds) { if (sounds != null) events.add("sound"); }
        @Override public void exitSounds() { events.add("stop"); }
    }

    @Test void initialEntryAndOnlyFirstTransitionRunOncePerFrame() throws Exception {
        ControllerMachine machine = new ControllerMachine(ControllerDefinition.parse(json("{'states':{'default':{'on_entry':['v.entry = v.entry + 1;'], 'on_exit':['v.exit = 1;'], 'transitions':[{'b':'1'},{'c':'1'}]},'b':{'on_entry':['v.b = 1;'],'sound_effects':[{'effect':'test'}],'transitions':[{'c':'1'}]},'c':{}}}")));
        Evaluation evaluation = new Evaluation();
        try (MolangScope ignored = new MolangScope(new HashMap<>())) {
            machine.update(100, evaluation);
            assertEquals("b", machine.state());
            assertEquals(Arrays.asList("v.entry = v.entry + 1;", "v.exit = 1;", "stop", "v.b = 1;", "sound"), evaluation.events);
            machine.update(100, evaluation);
            assertEquals("b", machine.state());
            assertEquals(5, evaluation.events.size());
            machine.update(101, evaluation);
            assertEquals("c", machine.state());
            assertEquals(1, evaluation.value("v.entry"));
        }
    }

    @Test void entityVariablesPersistWithoutLeakingAndReturnStopsExecution() {
        Evaluation evaluation = new Evaluation();
        Map<String, Double> first = new HashMap<>(), second = new HashMap<>();
        evaluation.variable("variable.counter", 42);
        try (MolangScope ignored = new MolangScope(first)) { evaluation.value("v.counter = v.counter + 1; return v.counter; v.counter = 99;"); }
        assertEquals(1d, first.get("variable.counter"));
        assertEquals(42, evaluation.value("v.counter"));
        try (MolangScope ignored = new MolangScope(second)) { assertEquals(0, evaluation.value("v.counter")); evaluation.value("v.counter = 7;"); }
        try (MolangScope ignored = new MolangScope(first)) { assertEquals(1, evaluation.value("v.counter")); }
        assertEquals(7d, second.get("variable.counter"));
    }

    @Test void comparisonsAndArithmeticFollowMolangPrecedence() {
        Evaluation evaluation = new Evaluation();
        assertEquals(1, evaluation.value("1 + 2 >= 3"));
        assertEquals(1, evaluation.value("2 <= 2 && 1 == 1"));
        assertEquals(0, evaluation.value("1 > 2 || 3 != 3"));
        assertEquals(1, evaluation.value("1 || 0 && 0"));
        assertEquals(7, evaluation.value("1 + 2 * 3"));
    }

    @Test void conditionalWithoutElseReturnsZeroAndWorksInsideBoneExpressions() {
        Evaluation evaluation = new Evaluation();
        evaluation.variable("query.ground_speed", 0);
        evaluation.variable("query.anim_time", 0);
        assertEquals(-10, evaluation.value("q.ground_speed < 0.8 ? -10"));
        assertEquals(10, evaluation.value("10 + (q.ground_speed > 0 ? math.sin(q.anim_time * 720) * 2)"));
        assertEquals(0, evaluation.value("v.bv = q.ground_speed > 0 ? math.cos(q.anim_time * 360) * 5; return v.bv;"));
        evaluation.variable("query.ground_speed", 1);
        assertEquals(0, evaluation.value("q.ground_speed < 0.8 ? -10"));
        assertEquals(5, evaluation.value("v.bv = q.ground_speed > 0 ? math.cos(q.anim_time * 360) * 5; return v.bv;"));
        assertEquals(-10, evaluation.value("-(q.ground_speed < 0.8 ? -17 : 10)"));
    }

    @Test void nestedConditionalsMatchTheirOwnElseBranches() {
        Evaluation evaluation = new Evaluation();
        assertEquals(2, evaluation.value("1 ? 0 ? 1 : 2 : 3"));
        assertEquals(3, evaluation.value("0 ? 0 ? 1 : 2 : 3"));
        assertEquals(4, evaluation.value("0 ? 1 : 0 ? 3 : 4"));
        assertEquals(2, evaluation.value("1 ? (0 ? 1 : 2)"));
        assertEquals(0, evaluation.value("0 ? (0 ? 1 : 2)"));
    }

    @Test void boneStringQueriesUseCurrentRenderContextAndKeepScalarScale() throws Exception {
        MolangParser parser = new MolangParser();
        java.util.concurrent.atomic.AtomicBoolean holding = new java.util.concurrent.atomic.AtomicBoolean();
        parser.setStringQueryResolver(call -> { assertEquals("ctrl.hold('mainhand', ':sword')", call); return holding.get() ? 1 : 0; });
        JsonObject clip = json("{'loop':true,'animation_length':1,'bones':{'Head':{}}}");
        clip.getAsJsonObject("bones").getAsJsonObject("Head").addProperty("scale", "ctrl.hold('mainhand', ':sword') ? 0 : 1");
        Animation a = JsonAnimationUtils.deserializeJsonToAnimation(new java.util.AbstractMap.SimpleEntry<>("a", clip), parser);
        assertEquals(1, a.boneAnimations.get(0).scaleKeyFrames.xKeyFrames.size());
        assertEquals(1, a.boneAnimations.get(0).scaleKeyFrames.xKeyFrames.get(0).getEndValue().get());
        holding.set(true);
        assertEquals(0, a.boneAnimations.get(0).scaleKeyFrames.xKeyFrames.get(0).getEndValue().get());
        assertEquals(-1, parser.parseExpression("-ctrl.hold('mainhand', ':sword')").get());
        MolangParser second = new MolangParser(); second.setStringQueryResolver(call -> 0);
        assertEquals(1, second.parseExpression("ctrl.hold('mainhand', ':sword') ? 0 : 1").get());
        assertEquals(0, a.boneAnimations.get(0).scaleKeyFrames.xKeyFrames.get(0).getEndValue().get());
    }

    @Test void independentMachinesUseOwnStateAndStateTime() throws Exception {
        ControllerDefinition definition = ControllerDefinition.parse(json("{'states':{'default':{'transitions':[{'run':'q.state_time >= 1'}],'blend_transition':0.5},'run':{}}}"));
        ControllerMachine first = new ControllerMachine(definition), second = new ControllerMachine(definition);
        Evaluation evaluation = new Evaluation();
        first.update(100, evaluation); second.update(110, evaluation);
        first.update(120, evaluation); second.update(120, evaluation);
        assertEquals("run", first.state()); assertEquals("default", second.state());
        assertEquals(0, first.blend(120)); assertEquals(0.5, first.blend(125)); assertEquals(1, first.blend(130));
    }

    @Test void weightsAndRemapCurveEvaluate() throws Exception {
        ControllerMachine machine = new ControllerMachine(ControllerDefinition.parse(json("{'states':{'default':{'variables':{'speed':{'input':'0.5','remap_curve':{'0':0,'1':2}}},'animations':['a',{'b':'v.speed'}]}}}")));
        Evaluation evaluation = new Evaluation();
        try (MolangScope ignored = new MolangScope(new HashMap<>())) {
            machine.update(0, evaluation);
            assertEquals(1d, machine.weights(evaluation).get("b"));
            assertEquals(1d, machine.weights(evaluation).get("a"));
        }
    }

    @Test void malformedTransitionFailsEarly() {
        assertThrows(IOException.class, () -> ControllerDefinition.parse(json("{'states':{'default':{'transitions':[{'missing':'1'}]}}}")));
        assertThrows(IOException.class, () -> ControllerDefinition.parse(json("{'initial_state':'missing','states':{'default':{}}}")));
        assertFalse(ControllerDefinition.isPlayerSlot("player.parallel_8"));
        assertTrue(ControllerDefinition.isPlayerSlot("player.post_use"));
    }

    @Test void simultaneousAnimationsBlendRotationPositionAndScale() throws Exception {
        MolangParser parser = new MolangParser();
        Animation a = animation("{'a':{'loop':true,'animation_length':1,'bones':{'Head':{'rotation':[20,0,0],'position':[4,0,0],'scale':[2,2,2]}}}}", parser);
        Map<String, Animation> animations = new HashMap<>(); animations.put("a", a); animations.put("b", a);
        Map<String, Double> weights = new HashMap<>(); weights.put("a", 0.25); weights.put("b", 0.5);
        AnimationSampler.Pose pose = AnimationSampler.sample(animations, weights, 0, parser).get("Head");
        assertEquals(Math.toRadians(-15), pose.values[0], 0.00001);
        assertEquals(3, pose.values[3], 0.00001); assertEquals(1.75, pose.values[6], 0.00001);
        Map<String, AnimationSampler.Pose> old = Collections.singletonMap("Head", pose);
        AnimationSampler.Pose faded = AnimationSampler.blend(old, Collections.emptyMap(), 0.5).get("Head");
        assertEquals(1.5, faded.values[3]); assertEquals(1.375, faded.values[6]);
    }

    @Test void samplerClampsFinalKeyframeAndInterpolatesMolangRotations() throws Exception {
        MolangParser parser = new MolangParser(); parser.setValue("query.test_angle", () -> 90);
        Animation a = animation("{'a':{'loop':false,'animation_length':2,'bones':{'Head':{'rotation':['q.test_angle',0,0],'position':{'0':[0,0,0],'1':[10,0,0]}}}}}", parser);
        assertEquals(Math.toRadians(-90), AnimationSampler.sampleAxis(a.boneAnimations.get(0).rotationKeyFrames.xKeyFrames, 10, true, 0), 0.00001);
        assertEquals(5, AnimationSampler.sampleAxis(a.boneAnimations.get(0).positionKeyFrames.xKeyFrames, 10, false, 0), 0.00001);
        assertEquals(10, AnimationSampler.sampleAxis(a.boneAnimations.get(0).positionKeyFrames.xKeyFrames, 200, false, 0), 0.00001);
        assertEquals(40, AnimationSampler.localTick(a, 200));
    }

    @Test void soundFramesArraysLoopOnceAndTimelineArraysRemainScripts() throws Exception {
        Animation a = animation("{'a':{'loop':true,'animation_length':1,'sound_effects':{'0':[{'effect':'first'},{'effect':'second'}],'0.5':{'effect':'third'}},'timeline':{'0':['v.a = 1;','v.b = 2;']}}}", new MolangParser());
        assertEquals(3, a.soundKeyFrames.size()); assertEquals("v.a = 1;", a.customInstructionKeyframes.get(0).getEventData());
        List<String> sounds = new ArrayList<>();
        AnimationEvents.emit(a, a.soundKeyFrames, -1, 0, sounds::add);
        assertEquals(2, sounds.size());
        AnimationEvents.emit(a, a.soundKeyFrames, 0, 0, sounds::add); assertEquals(2, sounds.size());
        AnimationEvents.emit(a, a.soundKeyFrames, 0, 10, sounds::add); assertEquals(3, sounds.size());
        AnimationEvents.emit(a, a.soundKeyFrames, 10, 20, sounds::add); assertEquals(5, sounds.size());
    }

    @Test void renderProcessorAddsParallelChannelsAndKeepsEntityStatesSeparate() throws Exception {
        ResourceLocation id = new ResourceLocation("yes_steve_model", "runtime_test/main");
        MolangParser parser = GeckoLibCache.getInstance().parser;
        AnimationFile file = new AnimationFile();
        file.putAnimation("a", animation("{'a':{'loop':true,'animation_length':1,'bones':{'Head':{'rotation':[20,0,0],'position':[4,0,0],'scale':[2,2,2]}}}}", parser));
        GeckoLibCache.getInstance().getAnimations().put(id, file);
        ModernAssets.Bundle assets = new ModernAssets.Bundle();
        assets.controllers.put("player.main", ControllerDefinition.parse(json("{'states':{'default':{'animations':['a'],'transitions':[{'done':'q.state_time >= 1'}]},'done':{}}}")));
        assets.controllers.put("player.parallel_0", ControllerDefinition.parse(json("{'states':{'default':{'animations':[{'a':0.5}]}}}")));
        ModernAssets.MODELS.put(id, assets);
        CustomPlayerEntity entity = new CustomPlayerEntity() {
            @Override public ResourceLocation getAnimation() { return id; }
            @Override public void registerControllers(com.elfmcys.yesstevemodel.geckolib3.core.manager.AnimationData data) {
                super.registerControllers(data);
                // Exercise the real modern controllers without invoking Minecraft GUI predicates.
                data.getAnimationControllers().entrySet().removeIf(entry -> !entry.getKey().equals("main_controller") && !entry.getKey().equals("parallel_0_controller"));
            }
        };
        AnimationProcessor<CustomPlayerEntity> processor = new AnimationProcessor<>(new IAnimatableModel<CustomPlayerEntity>() {
            @Override public AnimationProcessor getAnimationProcessor() { return null; }
            @Override public Animation getAnimation(String name, IAnimatable target) { return file.getAnimation(name); }
            @Override public void setMolangQueries(IAnimatable target, double time) { }
        });
        GeoBone bone = new GeoBone(); bone.name = "Head"; bone.saveInitialSnapshot(); processor.registerModelRenderer(bone);
        AnimationEvent<CustomPlayerEntity> event = new AnimationEvent<>(entity, 0, 0, 0, false, Collections.emptyList());
        try {
            processor.tickAnimation(entity, 100, 0, event, parser, true);
            assertEquals(6, bone.getPositionX(), 0.00001); assertEquals(3, bone.getScaleX(), 0.00001);
            assertEquals(Math.toRadians(-30), bone.getRotationX(), 0.00001);
            processor.tickAnimation(entity, 200, 10, event, parser, true);
            assertNotSame(entity.getFactory().getOrCreateAnimationData(100), entity.getFactory().getOrCreateAnimationData(200));
            processor.tickAnimation(entity, 100, 20, event, parser, true);
            assertEquals(2, bone.getPositionX(), 0.00001); assertEquals(1.5, bone.getScaleX(), 0.00001);
            processor.tickAnimation(entity, 200, 20, event, parser, true);
            assertEquals(6, bone.getPositionX(), 0.00001); assertEquals(3, bone.getScaleX(), 0.00001);
        } finally { ModernAssets.MODELS.remove(id); GeckoLibCache.getInstance().getAnimations().remove(id); }
    }

    @Test void renderProcessorAppliesCurvedTransitionToTheOutgoingPose() throws Exception {
        ResourceLocation id = new ResourceLocation("yes_steve_model", "curve_test/main");
        MolangParser parser = GeckoLibCache.getInstance().parser;
        AnimationFile file = new AnimationFile();
        file.putAnimation("old", animation("{'a':{'loop':true,'animation_length':1,'bones':{'Head':{'position':[2,0,0]}}}}", parser));
        file.putAnimation("next", animation("{'a':{'loop':true,'animation_length':1,'bones':{'Head':{'position':[12,0,0]}}}}", parser));
        GeckoLibCache.getInstance().getAnimations().put(id, file);
        ModernAssets.Bundle assets = new ModernAssets.Bundle();
        assets.controllers.put("player.main", ControllerDefinition.parse(json("{'states':{'default':{'animations':['old'],'transitions':[{'next':'q.state_time >= 1'}],'blend_transition':{'0':1,'0.1':0.9,'0.3':0}},'next':{'animations':['next']}}}")));
        ModernAssets.MODELS.put(id, assets);
        CustomPlayerEntity entity = new CustomPlayerEntity() {
            @Override public ResourceLocation getAnimation() { return id; }
            @Override public void registerControllers(com.elfmcys.yesstevemodel.geckolib3.core.manager.AnimationData data) {
                super.registerControllers(data);
                data.getAnimationControllers().entrySet().removeIf(entry -> !entry.getKey().equals("main_controller"));
            }
        };
        AnimationProcessor<CustomPlayerEntity> processor = new AnimationProcessor<>(new IAnimatableModel<CustomPlayerEntity>() {
            @Override public AnimationProcessor getAnimationProcessor() { return null; }
            @Override public Animation getAnimation(String name, IAnimatable target) { return file.getAnimation(name); }
            @Override public void setMolangQueries(IAnimatable target, double time) { }
        });
        GeoBone bone = new GeoBone(); bone.name = "Head"; bone.saveInitialSnapshot(); processor.registerModelRenderer(bone);
        AnimationEvent<CustomPlayerEntity> event = new AnimationEvent<>(entity, 0, 0, 0, false, Collections.emptyList());
        try {
            processor.tickAnimation(entity, 300, 0, event, parser, true);
            assertEquals(2, bone.getPositionX(), 1e-5);
            processor.tickAnimation(entity, 300, 20, event, parser, true);
            assertEquals(2, bone.getPositionX(), 1e-5);
            processor.tickAnimation(entity, 300, 22, event, parser, true);
            assertEquals(3, bone.getPositionX(), 1e-5);
            processor.tickAnimation(entity, 300, 24, event, parser, true);
            assertEquals(7.5, bone.getPositionX(), 1e-5);
            processor.tickAnimation(entity, 300, 26, event, parser, true);
            assertEquals(12, bone.getPositionX(), 1e-5);
        } finally { ModernAssets.MODELS.remove(id); GeckoLibCache.getInstance().getAnimations().remove(id); }
    }

    @Test void completedPlayOnceReleasesPoseButHoldLastFrameKeepsIt() throws Exception {
        MolangParser parser = new MolangParser();
        Animation a = animation("{'a':{'loop':false,'animation_length':1,'bones':{'Head':{'position':[4,0,0]}}}}", parser);
        Map<String, Animation> clips = Collections.singletonMap("a", a);
        assertTrue(AnimationSampler.sample(clips, Collections.singletonMap("a", 1d), 21, parser).isEmpty());
        a.loop = com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME;
        assertEquals(4, AnimationSampler.sample(clips, Collections.singletonMap("a", 1d), 21, parser).get("Head").values[3]);
    }

    @Test void pbrPreservesArgbAndRejectsMismatchedSizes() throws Exception {
        byte[] base = png(2, 2, 0xff123456), normal = png(2, 2, 0x7f89abcd);
        PbrImages.validate(base, normal, null);
        assertEquals(0x7f89abcd, PbrImages.layer(normal, 2, 2, 0).getRGB(0, 0));
        assertEquals(0xff7f7fff, PbrImages.layer(null, 2, 2, 0xff7f7fff).getRGB(0, 0));
        assertThrows(IOException.class, () -> PbrImages.validate(base, png(1, 1, 0), null));
    }

    @Test void missingOptifineKeepsOriginalVertexFormat() {
        net.minecraft.client.renderer.vertex.VertexFormat format = net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL;
        assertSame(format, com.elfmcys.yesstevemodel.client.texture.OptifinePbr.vertexFormat(format));
    }

    @Test void inMemorySoundPackReadsAndClearsResources() throws Exception {
        ModelSoundPack pack = new ModelSoundPack(); ResourceLocation id = new ResourceLocation(ModelSoundPack.DOMAIN, "model/test");
        byte[] bytes = {79, 103, 103, 83, 1}; pack.put(id, bytes);
        ResourceLocation ogg = new ResourceLocation(ModelSoundPack.DOMAIN, "sounds/model/test.ogg");
        assertTrue(pack.resourceExists(ogg));
        try (java.io.InputStream stream = pack.getInputStream(ogg)) { for (byte b : bytes) assertEquals(b, stream.read()); }
        pack.clear(); assertFalse(pack.resourceExists(ogg));
    }

    private static byte[] png(int width, int height, int argb) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); image.setRGB(0, 0, argb);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(); ImageIO.write(image, "png", bytes); return bytes.toByteArray();
    }
}
