/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.elfmcys.yesstevemodel.geckolib3.core.keyframe;

import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;

public class BoneAnimationQueue {
    public final IBone bone;
    public final AnimationPointQueue rotationXQueue = new AnimationPointQueue();
    public final AnimationPointQueue rotationYQueue = new AnimationPointQueue();
    public final AnimationPointQueue rotationZQueue = new AnimationPointQueue();
    public final AnimationPointQueue positionXQueue = new AnimationPointQueue();
    public final AnimationPointQueue positionYQueue = new AnimationPointQueue();
    public final AnimationPointQueue positionZQueue = new AnimationPointQueue();
    public final AnimationPointQueue scaleXQueue = new AnimationPointQueue();
    public final AnimationPointQueue scaleYQueue = new AnimationPointQueue();
    public final AnimationPointQueue scaleZQueue = new AnimationPointQueue();

    public BoneAnimationQueue(IBone bone) {
        this.bone = bone;
    }

    public IBone bone() {
        return this.bone;
    }

    public AnimationPointQueue rotationXQueue() {
        return this.rotationXQueue;
    }

    public AnimationPointQueue rotationYQueue() {
        return this.rotationYQueue;
    }

    public AnimationPointQueue rotationZQueue() {
        return this.rotationZQueue;
    }

    public AnimationPointQueue positionXQueue() {
        return this.positionXQueue;
    }

    public AnimationPointQueue positionYQueue() {
        return this.positionYQueue;
    }

    public AnimationPointQueue positionZQueue() {
        return this.positionZQueue;
    }

    public AnimationPointQueue scaleXQueue() {
        return this.scaleXQueue;
    }

    public AnimationPointQueue scaleYQueue() {
        return this.scaleYQueue;
    }

    public AnimationPointQueue scaleZQueue() {
        return this.scaleZQueue;
    }
}
