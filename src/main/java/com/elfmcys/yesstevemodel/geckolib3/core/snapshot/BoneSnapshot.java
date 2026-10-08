/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.elfmcys.yesstevemodel.geckolib3.core.snapshot;

import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;

public class BoneSnapshot {
    public String name;
    public float scaleValueX;
    public float scaleValueY;
    public float scaleValueZ;
    public float positionOffsetX;
    public float positionOffsetY;
    public float positionOffsetZ;
    public float rotationValueX;
    public float rotationValueY;
    public float rotationValueZ;
    public float mostRecentResetRotationTick = 0;
    public float mostRecentResetPositionTick = 0;
    public float mostRecentResetScaleTick = 0;
    public boolean isCurrentlyRunningRotationAnimation = true;
    public boolean isCurrentlyRunningPositionAnimation = true;
    public boolean isCurrentlyRunningScaleAnimation = true;
    private final IBone modelRenderer;

    public BoneSnapshot(IBone modelRenderer) {
        this.rotationValueX = modelRenderer.getRotationX();
        this.rotationValueY = modelRenderer.getRotationY();
        this.rotationValueZ = modelRenderer.getRotationZ();

        this.positionOffsetX = modelRenderer.getPositionX();
        this.positionOffsetY = modelRenderer.getPositionY();
        this.positionOffsetZ = modelRenderer.getPositionZ();

        this.scaleValueX = modelRenderer.getScaleX();
        this.scaleValueY = modelRenderer.getScaleY();
        this.scaleValueZ = modelRenderer.getScaleZ();

        this.modelRenderer = modelRenderer;
        this.name = modelRenderer.getName();
    }

    public BoneSnapshot(IBone modelRenderer, boolean dontSaveRotations) {
        if (dontSaveRotations) {
            this.rotationValueX = 0;
            this.rotationValueY = 0;
            this.rotationValueZ = 0;
        }

        this.rotationValueX = modelRenderer.getRotationX();
        this.rotationValueY = modelRenderer.getRotationY();
        this.rotationValueZ = modelRenderer.getRotationZ();

        this.positionOffsetX = modelRenderer.getPositionX();
        this.positionOffsetY = modelRenderer.getPositionY();
        this.positionOffsetZ = modelRenderer.getPositionZ();

        this.scaleValueX = modelRenderer.getScaleX();
        this.scaleValueY = modelRenderer.getScaleY();
        this.scaleValueZ = modelRenderer.getScaleZ();

        this.modelRenderer = modelRenderer;
        this.name = modelRenderer.getName();
    }

    public BoneSnapshot(BoneSnapshot snapshot) {
        this.scaleValueX = snapshot.scaleValueX;
        this.scaleValueY = snapshot.scaleValueY;
        this.scaleValueZ = snapshot.scaleValueZ;

        this.positionOffsetX = snapshot.positionOffsetX;
        this.positionOffsetY = snapshot.positionOffsetY;
        this.positionOffsetZ = snapshot.positionOffsetZ;

        this.rotationValueX = snapshot.rotationValueX;
        this.rotationValueY = snapshot.rotationValueY;
        this.rotationValueZ = snapshot.rotationValueZ;
        this.modelRenderer = snapshot.modelRenderer;
        this.name = snapshot.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        BoneSnapshot that = (BoneSnapshot) o;
        return this.name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
