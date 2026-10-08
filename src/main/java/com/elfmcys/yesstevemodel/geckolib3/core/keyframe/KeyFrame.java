/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.elfmcys.yesstevemodel.geckolib3.core.keyframe;

import com.elfmcys.yesstevemodel.geckolib3.core.easing.EasingType;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.List;
import java.util.Objects;

public class KeyFrame<T> {
    public EasingType easingType = EasingType.LINEAR;
    public List<Double> easingArgs = new DoubleArrayList();
    private Double length;
    private T startValue;
    private T endValue;

    public KeyFrame(Double length, T startValue, T endValue) {
        this.length = length;
        this.startValue = startValue;
        this.endValue = endValue;
    }

    public KeyFrame(Double length, T startValue, T endValue, EasingType easingType) {
        this.length = length;
        this.startValue = startValue;
        this.endValue = endValue;
        this.easingType = easingType;
    }

    public KeyFrame(Double length, T startValue, T endValue, EasingType easingType, List<Double> easingArgs) {
        this.length = length;
        this.startValue = startValue;
        this.endValue = endValue;
        this.easingType = easingType;
        this.easingArgs = easingArgs;
    }

    public Double getLength() {
        return this.length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public T getStartValue() {
        return this.startValue;
    }

    public void setStartValue(T startValue) {
        this.startValue = startValue;
    }

    public T getEndValue() {
        return this.endValue;
    }

    public void setEndValue(T endValue) {
        this.endValue = endValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.length, this.startValue, this.endValue);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof KeyFrame && this.hashCode() == obj.hashCode();
    }
}
