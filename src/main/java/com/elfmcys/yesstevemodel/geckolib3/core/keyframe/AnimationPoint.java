/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.elfmcys.yesstevemodel.geckolib3.core.keyframe;

import com.github.bsideup.jabel.Desugar;

/**
 * @param currentTick         动画插值中要从中获取的当前 tick
 * @param animationEndTick    当前动画结束的 tick
 * @param animationStartValue 动画起始值
 * @param animationEndValue   动画结束值
 * @param keyframe            当前关键帧
 */
@SuppressWarnings("rawtypes")
@Desugar
public record AnimationPoint(
        KeyFrame keyframe,
        double currentTick,
        double animationEndTick,
        double animationStartValue,
        double animationEndValue
) {
    @Override
    public String toString() {
        return "Tick: " + this.currentTick + " | End Tick: " + this.animationEndTick + " | Start Value: " + this.animationStartValue
                + " | End Value: " + this.animationEndValue;
    }
}
