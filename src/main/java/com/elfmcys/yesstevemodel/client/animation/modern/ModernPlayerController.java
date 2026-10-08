package com.elfmcys.yesstevemodel.client.animation.modern;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimationState;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.AnimationPoint;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.AnimationPointQueue;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.BoneAnimationQueue;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.expressions.MolangExpression;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneSnapshot;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.model.modern.AnimationEvents;
import com.elfmcys.yesstevemodel.model.modern.AnimationSampler;
import com.elfmcys.yesstevemodel.model.modern.ControllerDefinition;
import com.elfmcys.yesstevemodel.model.modern.ControllerMachine;
import com.elfmcys.yesstevemodel.model.modern.MolangScope;
import com.google.gson.JsonElement;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ModernPlayerController extends AnimationController<CustomPlayerEntity> {
    /** Shared by controllers for one AnimationData instance, never by different players. */
    public static final class EntityState {
        final Map<String, Double> variables = new HashMap<>();
        final com.elfmcys.yesstevemodel.model.modern.SecondOrderDynamics dynamics = new com.elfmcys.yesstevemodel.model.modern.SecondOrderDynamics();
        private Map<String, Double> configured = java.util.Collections.emptyMap();
        private ResourceLocation model;
        private UUID player;
        private ModernAssets.Bundle bundle;
        private long revision;

        long sync(CustomPlayerEntity entity) {
            ResourceLocation id = entity.getAnimation();
            UUID uuid = entity.getPlayer() == null ? null : entity.getPlayer().getUniqueID();
            ModernAssets.Bundle next = ModernAssets.MODELS.get(id);
            if (!id.equals(model) || !java.util.Objects.equals(uuid, player) || next != bundle) {
                model = id; player = uuid; bundle = next;
                variables.clear(); dynamics.clear(); configured = java.util.Collections.emptyMap(); revision++;
            }
            if (entity.getPlayer() != null) com.elfmcys.yesstevemodel.event.CapabilityEvent.getModelInfoCap(entity.getPlayer()).ifPresent(cap -> {
                Map<String, Double> settings = cap.getModelSettings();
                if (!settings.equals(configured)) { variables.putAll(settings); configured = new HashMap<>(settings); }
            });
            return revision;
        }
    }

    private final String slot;
    private final EntityState entityState;
    private long revision = -1;
    private ControllerMachine machine;
    private final ModelSounds.Context stateSounds = new ModelSounds.Context();
    private final ModelSounds.Context legacySounds = new ModelSounds.Context();
    private final Map<String, ModelSounds.Context> animationSounds = new HashMap<>();
    private final Map<String, Double> eventTimes = new HashMap<>();
    private final Map<String, MolangExpression> expressions = new HashMap<>();
    private final Set<String> warned = new HashSet<>();
    private final ControllerQueries queries = new ControllerQueries();
    private Map<String, AnimationSampler.Pose> lastPose = Collections.emptyMap();
    private Map<String, AnimationSampler.Pose> outgoing = Collections.emptyMap();
    private Animation legacyAnimation;
    private double legacyEventTick = -1;
    private double legacyAdjustedTick = -1;
    private long animationRequest = Long.MIN_VALUE;

    public void acceptAnimationRequest(long request) {
        if (request == animationRequest) return;
        animationRequest = request;
        markNeedsReload(); clearAnimationCache();
    }

    @Override public boolean isAdditive() { return machine != null && slot.contains("parallel_"); }

    public ModernPlayerController(CustomPlayerEntity entity, String name, String slot, float transition, IAnimationPredicate<CustomPlayerEntity> fallback, EntityState state) {
        super(entity, name, transition, fallback);
        this.slot = slot;
        this.entityState = state;
        registerSoundListener(event -> {
            if (event.getAnimationTick() < legacyEventTick || legacyAnimation != super.getCurrentAnimation()) legacySounds.stop();
            legacyEventTick = event.getAnimationTick();
            legacyAnimation = super.getCurrentAnimation();
            ModelSounds.play(entity.getAnimation(), entity.getPlayer(), event.sound, legacySounds);
        });
        registerCustomInstructionListener(event -> new Evaluation(GeckoLibCache.getInstance().parser).script(event.instructions));
    }

    @Override public double adjustTick(double tick) {
        double adjusted = super.adjustTick(tick);
        if (adjusted < legacyAdjustedTick) legacySounds.stop();
        legacyAdjustedTick = adjusted;
        return adjusted;
    }

    @Override
    public void process(double tick, AnimationEvent<CustomPlayerEntity> event, List<IBone> bones, Map<String, Pair<IBone, BoneSnapshot>> snapshots, MolangParser parser, boolean crash) {
        long nextRevision = entityState.sync(animatable);
        if (nextRevision != revision) {
            revision = nextRevision;
            stopAnimationSounds(); stateSounds.stop(); legacySounds.stop();
            eventTimes.clear(); lastPose = outgoing = Collections.emptyMap();
            ControllerDefinition definition = entityState.bundle == null ? null : entityState.bundle.controllers.get(slot);
            machine = definition == null ? null : new ControllerMachine(definition);
            markNeedsReload(); clearAnimationCache();
        }
        try (MolangScope ignored = new MolangScope(entityState.variables);
             com.elfmcys.yesstevemodel.model.modern.MolangRuntime runtime = new com.elfmcys.yesstevemodel.model.modern.MolangRuntime(
                     entityState.bundle == null ? Collections.emptyMap() : entityState.bundle.functions, entityState.dynamics, tick / 20)) {
            if (machine == null || animatable.hasPreviewAnimation()) {
                super.process(tick, event, bones, snapshots, parser, crash);
                if (super.getCurrentAnimation() != legacyAnimation || super.getAnimationState() == AnimationState.STOPPED) legacySounds.stop();
                return;
            }
            getBoneAnimationQueues().clear();
            Map<String, Animation> animations = GeckoLibCache.getInstance().getAnimations().get(animatable.getAnimation()).animations();
            Evaluation evaluation = new Evaluation(parser);
            double elapsed = machine.state() == null ? 0 : machine.elapsed(tick);
            evaluation.variable("query.state_time", elapsed / 20);
            evaluation.variable("query.anim_time", elapsed / 20);
            Map<String, Double> weights = machine.weights(evaluation);
            boolean all = !weights.isEmpty(), any = false;
            for (String name : weights.keySet()) {
                Animation animation = animations.get(name);
                boolean finished = animation != null && !animation.loop.isRepeatingAfterEnd() && elapsed >= animation.animationLength;
                all &= finished; any |= finished;
            }
            evaluation.variable("query.all_animations_finished", all ? 1 : 0);
            evaluation.variable("query.any_animation_finished", any ? 1 : 0);
            machine.update(tick, evaluation);
            if (machine.changed()) {
                outgoing = lastPose;
                stopAnimationSounds(); eventTimes.clear();
            }
            elapsed = machine.elapsed(tick);
            weights = machine.weights(evaluation);
            for (Map.Entry<String, Double> entry : weights.entrySet()) {
                Animation animation = animations.get(entry.getKey());
                if (animation == null) { warn("Missing animation " + entry.getKey()); continue; }
                if (entry.getValue() <= 0) continue;
                double previous = eventTimes.getOrDefault(entry.getKey(), -1d);
                ModelSounds.Context context = animationSounds.computeIfAbsent(entry.getKey(), key -> new ModelSounds.Context());
                if (previous >= 0 && animation.animationLength > 0 && (Math.floor(elapsed / animation.animationLength) != Math.floor(previous / animation.animationLength))) context.stop();
                final ModelSounds.Context audio = context;
                parser.setValue("query.anim_time", AnimationSampler.localTick(animation, elapsed) / 20);
                AnimationEvents.emit(animation, animation.soundKeyFrames, previous, elapsed, sound -> ModelSounds.play(animatable.getAnimation(), animatable.getPlayer(), sound, audio));
                AnimationEvents.emit(animation, animation.customInstructionKeyframes, previous, elapsed, evaluation::script);
                if (!animation.loop.isRepeatingAfterEnd() && elapsed >= animation.animationLength) context.stop();
                eventTimes.put(entry.getKey(), elapsed);
            }
            lastPose = AnimationSampler.sample(animations, weights, elapsed, parser);
            double blend = machine.blend(tick);
            if (blend != 1) lastPose = AnimationSampler.blend(outgoing, lastPose, blend);
            for (IBone bone : bones) {
                AnimationSampler.Pose pose = lastPose.get(bone.getName());
                if (pose == null) continue;
                BoneAnimationQueue queue = new BoneAnimationQueue(bone);
                AnimationPointQueue[] channels = {queue.rotationXQueue, queue.rotationYQueue, queue.rotationZQueue, queue.positionXQueue, queue.positionYQueue, queue.positionZQueue, queue.scaleXQueue, queue.scaleYQueue, queue.scaleZQueue};
                for (int i = 0; i < 9; i++) if (pose.channels[i / 3]) channels[i].add(new AnimationPoint(null, 0, 0, pose.values[i], pose.values[i]));
                getBoneAnimationQueues().put(bone.getName(), queue);
            }
        }
    }

    private void stopAnimationSounds() { for (ModelSounds.Context context : animationSounds.values()) context.stop(); animationSounds.clear(); }
    private void warn(String message) { if (warned.add(message)) YesSteveModel.LOGGER.warn("Modern controller {}: {}", slot, message); }

    private final class Evaluation implements ControllerMachine.Evaluation {
        final MolangParser parser;
        Evaluation(MolangParser parser) { this.parser = parser; }
        @Override public double value(String expression) {
            expression = queries.resolve(expression, animatable.getPlayer(), ModernPlayerController.this::warn);
            try {
                MolangExpression compiled = expressions.get(expression);
                if (compiled == null) { compiled = parser.parseExpression(expression); expressions.put(expression, compiled); }
                return compiled.get();
            } catch (Exception e) { expressions.put(expression, MolangParser.ZERO); warn("Unsupported Molang: " + expression); return 0; }
        }
        @Override public void variable(String name, double value) { parser.setValue(name, () -> value); }
        void script(String source) { if (!source.trim().startsWith("/")) value(source); else warn("Command scripts are unsupported: " + source); }
        @Override public void scripts(JsonElement scripts) {
            if (scripts == null) return;
            if (scripts.isJsonArray()) for (JsonElement script : scripts.getAsJsonArray()) script(script.getAsString());
            else script(scripts.getAsString());
        }
        @Override public void enterSounds(JsonElement sounds) { ModelSounds.playState(animatable.getAnimation(), animatable.getPlayer(), sounds, stateSounds); }
        @Override public void exitSounds() { stateSounds.stop(); }
    }
}
