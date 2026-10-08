package com.elfmcys.yesstevemodel.geckolib3.core.processor;

public class PointData {
    public final float[] applied = {0, 0, 0, 0, 0, 0, 1, 1, 1};
    // Parallel scale is a persistent multiplier (including form visibility),
    // while ordinary pose/action controllers replace the base scale.
    public final float[] scaleBase = {1, 1, 1};
    public final float[] scaleParallel = {1, 1, 1};
    public float rotationValueX;
    public float rotationValueY;
    public float rotationValueZ;
}
