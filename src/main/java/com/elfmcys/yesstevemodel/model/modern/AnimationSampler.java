package com.elfmcys.yesstevemodel.model.modern;

import com.elfmcys.yesstevemodel.geckolib3.core.ConstantValue;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.easing.EasingType;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.AnimationPoint;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.BoneAnimation;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.KeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.VectorKeyFrameList;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser;
import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil;
import com.elfmcys.yesstevemodel.mclib.math.IValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Concurrent weighted animations: rotations/positions add; scale adds around one. */
public final class AnimationSampler {
    public static final class Pose {
        public final double[] values = {0, 0, 0, 0, 0, 0, 1, 1, 1};
        public final boolean[] channels = new boolean[3];
    }

    private AnimationSampler() {}

    public static double localTick(Animation animation, double elapsed) {
        double length = animation.animationLength;
        if (length <= 0 || !Double.isFinite(length)) return 0;
        return animation.loop.isRepeatingAfterEnd() ? elapsed % length : Math.min(elapsed, length);
    }

    public static Map<String, Pose> sample(Map<String, Animation> animations, Map<String, Double> weights, double elapsed, MolangParser parser) {
        Map<String, Pose> poses = new HashMap<>();
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            Animation animation = animations.get(entry.getKey());
            if (animation == null || entry.getValue() <= 0) continue;
            if (!animation.loop.isRepeatingAfterEnd() && animation.loop != ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME
                    && elapsed >= animation.animationLength) continue;
            double tick = localTick(animation, elapsed);
            parser.setValue("query.anim_time", () -> tick / 20);
            for (BoneAnimation bone : animation.boneAnimations) {
                Pose pose = poses.computeIfAbsent(bone.boneName, name -> new Pose());
                add(pose, bone.rotationKeyFrames, tick, entry.getValue(), 0);
                add(pose, bone.positionKeyFrames, tick, entry.getValue(), 1);
                add(pose, bone.scaleKeyFrames, tick, entry.getValue(), 2);
            }
        }
        return poses;
    }

    private static void add(Pose pose, VectorKeyFrameList<KeyFrame<IValue>> frames, double tick, double weight, int channel) {
        if (frames == null || frames.xKeyFrames.isEmpty() || frames.yKeyFrames.isEmpty() || frames.zKeyFrames.isEmpty()) return;
        pose.channels[channel] = true;
        double baseline = channel == 2 ? 1 : 0;
        pose.values[channel * 3] += weight * (sampleAxis(frames.xKeyFrames, tick, channel == 0, 0) - baseline);
        pose.values[channel * 3 + 1] += weight * (sampleAxis(frames.yKeyFrames, tick, channel == 0, 1) - baseline);
        pose.values[channel * 3 + 2] += weight * (sampleAxis(frames.zKeyFrames, tick, channel == 0, 2) - baseline);
    }

    public static double sampleAxis(List<KeyFrame<IValue>> frames, double tick, boolean rotation, int axis) {
        double start = 0;
        KeyFrame<IValue> current = frames.get(frames.size() - 1);
        double local = current.getLength();
        for (KeyFrame<IValue> frame : frames) {
            if (tick < start + frame.getLength()) {
                current = frame;
                local = Math.max(0, tick - start);
                break;
            }
            start += frame.getLength();
        }
        double from = value(current.getStartValue(), rotation, axis);
        double to = value(current.getEndValue(), rotation, axis);
        return MathUtil.lerpValues(new AnimationPoint(current, local, current.getLength(), from, to), EasingType.NONE, null);
    }

    private static double value(IValue value, boolean rotation, int axis) {
        double result = value.get();
        if (rotation && !(value instanceof ConstantValue)) result = Math.toRadians(result) * (axis == 2 ? 1 : -1);
        return result;
    }

    public static Map<String, Pose> blend(Map<String, Pose> old, Map<String, Pose> next, double amount) {
        Map<String, Pose> result = new HashMap<>();
        java.util.Set<String> bones = new java.util.HashSet<>(old.keySet());
        bones.addAll(next.keySet());
        for (String bone : bones) {
            Pose from = old.getOrDefault(bone, new Pose()), to = next.getOrDefault(bone, new Pose()), pose = new Pose();
            for (int i = 0; i < 9; i++) pose.values[i] = from.values[i] + amount * (to.values[i] - from.values[i]);
            for (int i = 0; i < 3; i++) pose.channels[i] = from.channels[i] || to.channels[i];
            result.put(bone, pose);
        }
        return result;
    }
}
