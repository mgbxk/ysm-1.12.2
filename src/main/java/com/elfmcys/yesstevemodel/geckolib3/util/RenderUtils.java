package com.elfmcys.yesstevemodel.geckolib3.util;

import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone;
import net.minecraft.client.renderer.GlStateManager;

import java.util.List;

public final class RenderUtils {
    public static void translateMatrixToBone(GeoBone bone) {
        GlStateManager.translate(-bone.getPositionX() / 16f, bone.getPositionY() / 16f, bone.getPositionZ() / 16f);
    }

    public static void rotateMatrixAroundBone(GeoBone bone) {
        if (bone.getRotationZ() != 0.0F) {
            GlStateManager.rotate(MathUtil.radiansToDegrees(bone.getRotationZ()), 0, 0, 1);
        }
        if (bone.getRotationY() != 0.0F) {
            GlStateManager.rotate(MathUtil.radiansToDegrees(bone.getRotationY()), 0, 1, 0);
        }
        if (bone.getRotationX() != 0.0F) {
            GlStateManager.rotate(MathUtil.radiansToDegrees(bone.getRotationX()), 1, 0, 0);
        }
    }

    /**
     * 如果缩放全为 0，则返回 true
     */
    public static boolean scaleMatrixForBone(GeoBone bone) {
        float scaleX = bone.getScaleX();
        float scaleY = bone.getScaleY();
        float scaleZ = bone.getScaleZ();
        GlStateManager.scale(scaleX, scaleY, scaleZ);
        return scaleX == 0 && scaleY == 0 && scaleZ == 0;
    }

    public static void translateToPivotPoint(GeoBone bone) {
        GlStateManager.translate(bone.rotationPointX / 16f, bone.rotationPointY / 16f, bone.rotationPointZ / 16f);
    }

    public static void translateAwayFromPivotPoint(GeoBone bone) {
        GlStateManager.translate(-bone.rotationPointX / 16f, -bone.rotationPointY / 16f, -bone.rotationPointZ / 16f);
    }

    /**
     * 如果缩放为 0，则返回 true
     */
    public static boolean prepMatrixForBone(GeoBone bone) {
        translateMatrixToBone(bone);
        translateToPivotPoint(bone);
        rotateMatrixAroundBone(bone);
        boolean scaleAllIsZero = scaleMatrixForBone(bone);
        translateAwayFromPivotPoint(bone);
        return scaleAllIsZero;
    }

    public static boolean prepMatrixForLocator(List<GeoBone> bones) {
        boolean scaleCheck = false;
        int size = bones.size();
        for (int i = 0; i < size - 1; i++) {
            boolean result = RenderUtils.prepMatrixForBone(bones.get(i));
            if (result) {
                scaleCheck = true;
            }
        }
        GeoBone lastBone = bones.get(size - 1);
        RenderUtils.translateMatrixToBone(lastBone);
        RenderUtils.translateToPivotPoint(lastBone);
        RenderUtils.rotateMatrixAroundBone(lastBone);
        RenderUtils.scaleMatrixForBone(lastBone);
        return scaleCheck;
    }
}
