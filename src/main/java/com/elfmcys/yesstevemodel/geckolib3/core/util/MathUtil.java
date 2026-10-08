package com.elfmcys.yesstevemodel.geckolib3.core.util;

import com.elfmcys.yesstevemodel.geckolib3.core.easing.EasingManager;
import com.elfmcys.yesstevemodel.geckolib3.core.easing.EasingType;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.AnimationPoint;

import java.util.function.DoubleUnaryOperator;

public class MathUtil {
    private static final float DEGREES_TO_RADIANS = (float) Math.PI / 180F;
    private static final float RADIANS_TO_DEGREES = 180F / (float) Math.PI;

    /**
     * 对一个 AnimationPoint 进行线性插值计算
     *
     * @param animationPoint 动画信息
     * @return 线性插值
     */
    @SuppressWarnings({"ConstantValue", "unchecked"})
    public static float lerpValues(AnimationPoint animationPoint, EasingType easingType, DoubleUnaryOperator customEasingMethod) {
        if (animationPoint.currentTick() >= animationPoint.animationEndTick()) {
            return (float) animationPoint.animationEndValue();
        }
        if (animationPoint.currentTick() == 0 && animationPoint.animationEndTick() == 0) {
            return (float) animationPoint.animationEndValue();
        }
        if (easingType == EasingType.CUSTOM && customEasingMethod != null) {
            return lerpValues(customEasingMethod.applyAsDouble(animationPoint.currentTick() / animationPoint.animationEndTick()),
                    animationPoint.animationStartValue(), animationPoint.animationEndValue());
        } else if (easingType == EasingType.NONE && animationPoint.keyframe() != null) {
            easingType = animationPoint.keyframe().easingType;
        }
        double ease = EasingManager.ease(animationPoint.currentTick() / animationPoint.animationEndTick(), easingType,
                animationPoint.keyframe() == null ? null : animationPoint.keyframe().easingArgs);
        return lerpValues(ease, animationPoint.animationStartValue(), animationPoint.animationEndValue());
    }

    public static float lerpValues(double percentCompleted, double startValue, double endValue) {
        return (float) lerp(percentCompleted, startValue, endValue);
    }

    public static double lerp(double pct, double start, double end) {
        return start + pct * (end - start);
    }

    public static float degreesToRadians(float degrees) {
        return degrees * DEGREES_TO_RADIANS;
    }

    public static float radiansToDegrees(float degrees) {
        return degrees * RADIANS_TO_DEGREES;
    }
}
