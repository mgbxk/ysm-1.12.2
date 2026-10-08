package com.elfmcys.yesstevemodel.model.modern;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.animation.modern.ModernPlayerController;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatableModel;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationBuilder;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.manager.AnimationData;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.AnimationProcessor;
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationFile;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.geckolib3.util.json.JsonAnimationUtils;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PlayerPoseRegressionTest {
    private static final ResourceLocation ID = new ResourceLocation("yes_steve_model", "wine_pose_regression/main");

    private static class WineFox extends CustomPlayerEntity {
        String action;
        final com.elfmcys.yesstevemodel.capability.ModelInfoCapability requests = new com.elfmcys.yesstevemodel.capability.ModelInfoCapability();
        void request(String clip) { action = clip; requests.playAnimation(clip); }
        @Override public ResourceLocation getAnimation() { return ID; }
        @Override public void registerControllers(AnimationData data) {
            ModernPlayerController.EntityState state = new ModernPlayerController.EntityState();
            data.addAnimationController(new ModernPlayerController(this, "parallel_1_controller", "player.parallel_1", 0, event -> {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("parallel1"));
                return PlayState.CONTINUE;
            }, state));
            data.addAnimationController(new ModernPlayerController(this, "cap_controller", "legacy.cap", 2, event -> {
                if (action == null) return PlayState.STOP;
                ((ModernPlayerController) event.getController()).acceptAnimationRequest(requests.getAnimationRequest());
                event.getController().setAnimation(new AnimationBuilder().addAnimation(action));
                return PlayState.CONTINUE;
            }, state));
        }
    }

    private static class Fixture implements AutoCloseable {
        final AnimationFile file = new AnimationFile();
        final AnimationProcessor<CustomPlayerEntity> processor;
        final AnimationController.ModelFetcher<CustomPlayerEntity> fetcher;
        final Map<String, GeoBone> bones = new LinkedHashMap<>();
        Fixture() throws Exception {
            for (String name : new String[]{"main", "extra"}) {
                try (InputStreamReader input = new InputStreamReader(getClass().getResourceAsStream("/assets/yes_steve_model/builtin/wine_fox/" + name + ".animation.json"), StandardCharsets.UTF_8)) {
                    JsonObject json = YesSteveModel.GSON.fromJson(input, JsonObject.class);
                    for (Map.Entry<String, com.google.gson.JsonElement> entry : json.getAsJsonObject("animations").entrySet()) {
                        Animation clip = JsonAnimationUtils.deserializeJsonToAnimation(entry, GeckoLibCache.getInstance().parser);
                        file.putAnimation(entry.getKey(), clip);
                        for (com.elfmcys.yesstevemodel.geckolib3.core.keyframe.BoneAnimation bone : clip.boneAnimations) bones.computeIfAbsent(bone.boneName, key -> { GeoBone b = new GeoBone(); b.name = key; return b; });
                    }
                }
            }
            IAnimatableModel<CustomPlayerEntity> model = new IAnimatableModel<CustomPlayerEntity>() {
                @Override public AnimationProcessor getAnimationProcessor() { return null; }
                @Override public Animation getAnimation(String name, IAnimatable target) { return file.getAnimation(name); }
                @Override public void setMolangQueries(IAnimatable target, double time) { }
            };
            processor = new AnimationProcessor<>(model);
            bones.values().forEach(processor::registerModelRenderer);
            fetcher = target -> target instanceof WineFox ? model : null;
            AnimationController.addModelFetcher(fetcher);
            GeckoLibCache.getInstance().getAnimations().put(ID, file);
        }
        void tick(WineFox entity, int id, double time) {
            processor.tickAnimation(entity, id, time, new AnimationEvent<>(entity, 0, 0, 0, false, Collections.emptyList()), GeckoLibCache.getInstance().parser, true);
        }
        void form(boolean fox) {
            assertEquals(fox ? 0 : 1, bones.get("AllBody").getScaleX(), 0.00001, "human visibility");
            assertEquals(fox ? 1 : 0, bones.get("FOX").getScaleX(), 0.00001, "fox visibility");
        }
        void transform(WineFox entity, int id, int start) {
            entity.request("extra0");
            for (int t = start; t <= start + 65; t++) tick(entity, id, t);
            entity.action = null;
            tick(entity, id, start + 66);
        }
        @Override public void close() { AnimationController.removeModelFetcher(fetcher); GeckoLibCache.getInstance().getAnimations().remove(ID); }
    }

    @Test void wineFoxKeepsTransformedFormThroughoutEveryActionAndCanTransformBack() throws Exception {
        try (Fixture fixture = new Fixture()) {
            WineFox entity = new WineFox();
            fixture.transform(entity, 101, 0);
            fixture.form(true);
            int time = 67;
            for (int action = 1; action <= 7; action++) {
                entity.request("extra" + action);
                for (int t = 0; t < 165; t++) { fixture.tick(entity, 101, time++); fixture.form(true); }
                entity.action = null;
                fixture.tick(entity, 101, time++);
                fixture.form(true);
            }
            fixture.transform(entity, 101, time);
            fixture.form(false);
        }
    }

    @Test void previewAndWorldWithTheSamePlayerIdDoNotSharePoseOrSkipEachOther() throws Exception {
        try (Fixture fixture = new Fixture()) {
            WineFox world = new WineFox(), preview = new WineFox();
            fixture.transform(world, 123, 0);
            fixture.tick(world, 123, 100);
            fixture.form(true);
            fixture.tick(preview, 123, 100);
            fixture.form(false);
            // Simulate the render-only head adjustment made after the controller pose.
            fixture.bones.get("Head").setRotationY(1.5f);
            fixture.tick(world, 123, 100);
            fixture.form(true);
            assertEquals(0, fixture.bones.get("Head").getRotationY(), 0.00001);
            fixture.tick(preview, 123, 100);
            fixture.form(false);
        }
    }

    @Test void readOnlyCurrentPreviewRestoresFormWithoutRunningTransformationTimeline() throws Exception {
        try (Fixture fixture = new Fixture()) {
            WineFox entity = new WineFox();
            fixture.transform(entity, 789, 0);
            fixture.form(true);
            entity.request("extra0");
            for (int frame = 0; frame < 200; frame++) {
                fixture.bones.get("AllBody").setScaleX(1);
                assertTrue(fixture.processor.restoreCachedPose(entity, 789));
                fixture.form(true);
            }
            // Only actual animation ticks may run the next transformation.
            fixture.transform(entity, 789, 67);
            fixture.form(false);
        }
    }

    @Test void animationRequestsDistinguishSameActionAndSurviveCapabilitySync() {
        com.elfmcys.yesstevemodel.capability.ModelInfoCapability cap = new com.elfmcys.yesstevemodel.capability.ModelInfoCapability();
        cap.playAnimation("extra0");
        long first = cap.getAnimationRequest();
        cap.playAnimation("extra0");
        assertEquals(first + 1, cap.getAnimationRequest());
        com.elfmcys.yesstevemodel.capability.ModelInfoCapability copy = new com.elfmcys.yesstevemodel.capability.ModelInfoCapability();
        copy.deserializeNBT(cap.serializeNBT());
        assertEquals(cap.getAnimationRequest(), copy.getAnimationRequest());
        assertEquals("extra0", copy.getAnimation());
        copy.copyFrom(cap);
        assertEquals(cap.getAnimationRequest(), copy.getAnimationRequest());
    }
}
