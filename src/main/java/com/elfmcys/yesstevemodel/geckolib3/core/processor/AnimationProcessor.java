package com.elfmcys.yesstevemodel.geckolib3.core.processor;

import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatableModel;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.AnimationPoint;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.BoneAnimationQueue;
import com.elfmcys.yesstevemodel.geckolib3.core.manager.AnimationData;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser;
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneSnapshot;
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.DirtyTracker;
import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@SuppressWarnings({"rawtypes", "unchecked"})
public class AnimationProcessor<T extends IAnimatable> {
    private final List<IBone> modelRendererList = new ObjectArrayList<>();
    private final Map<AnimationData, CachedPose> poses = new WeakHashMap<>();
    private final IAnimatableModel animatedModel;
    public boolean reloadAnimations = false;

    public AnimationProcessor(IAnimatableModel animatedModel) {
        this.animatedModel = animatedModel;
    }

    /** Read-only current-player GUI preview; never advances controllers or timelines. */
    public boolean restoreCachedPose(IAnimatable entity, int uniqueID) {
        CachedPose pose = this.poses.get(entity.getFactory().getOrCreateAnimationData(uniqueID));
        if (pose == null || !pose.matches(this.modelRendererList)) return false;
        pose.restore(this.modelRendererList);
        return true;
    }

    public void tickAnimation(IAnimatable entity, int uniqueID, double seekTime, AnimationEvent<T> event, MolangParser parser, boolean crashWhenCantFindBone) {
        // 每一个动画都有自己的动画数据（AnimationData）
        // 这样多个动画就能互相独立了
        AnimationData manager = entity.getFactory().getOrCreateAnimationData(uniqueID);
        CachedPose pose = this.poses.get(manager);
        if (pose != null && !pose.matches(this.modelRendererList)) {
            // Model changes and resource reloads must not keep snapshots of old bones.
            manager.clearSnapshotCache();
            manager.getAnimationControllers().values().forEach(controller -> controller.markNeedsReload());
            pose = null;
        }
        if (pose == null) {
            pose = new CachedPose(this.modelRendererList);
            this.poses.put(manager, pose);
        }
        // Geometry is shared by world players, GUI cards and the current-player
        // preview. Restore this instance before both sampling and repeated draws.
        pose.restore(this.modelRendererList);
        if (pose.tick == seekTime && !this.reloadAnimations) return;
        // 追踪哪些骨骼应用了动画，并最终将没有动画的骨骼设置为默认值
        Map<String, DirtyTracker> modelTracker = this.createNewDirtyTracker();
        // 存储每个骨骼的 rotation/position/scale
        this.updateBoneSnapshots(manager.getBoneSnapshotCollection());
        Map<String, Pair<IBone, BoneSnapshot>> boneSnapshots = manager.getBoneSnapshotCollection();
        HashMap<String, PointData> pointDataGroup = Maps.newHashMap();
        for (AnimationController<T> controller : manager.getAnimationControllers().values()) {
            if (this.reloadAnimations) {
                controller.markNeedsReload();
                controller.getBoneAnimationQueues().clear();
            }
            controller.isJustStarting = manager.isFirstTick;
            // 将当前控制器设置为动画测试事件
            event.setController(controller);
            // 处理动画并向点队列添加新值
            controller.process(seekTime, event, this.modelRendererList, boneSnapshots, parser, crashWhenCantFindBone);
            // 遍历每个骨骼，并对属性进行插值计算
            for (BoneAnimationQueue boneAnimation : controller.getBoneAnimationQueues().values()) {
                IBone bone = boneAnimation.bone();
                BoneSnapshot snapshot = boneSnapshots.get(bone.getName()).getRight();
                BoneSnapshot initialSnapshot = bone.getInitialSnapshot();
                pointDataGroup.putIfAbsent(bone.getName(), new PointData());
                PointData pointData = pointDataGroup.get(bone.getName());

                AnimationPoint rXPoint = boneAnimation.rotationXQueue().poll();
                AnimationPoint rYPoint = boneAnimation.rotationYQueue().poll();
                AnimationPoint rZPoint = boneAnimation.rotationZQueue().poll();

                AnimationPoint pXPoint = boneAnimation.positionXQueue().poll();
                AnimationPoint pYPoint = boneAnimation.positionYQueue().poll();
                AnimationPoint pZPoint = boneAnimation.positionZQueue().poll();

                AnimationPoint sXPoint = boneAnimation.scaleXQueue().poll();
                AnimationPoint sYPoint = boneAnimation.scaleYQueue().poll();
                AnimationPoint sZPoint = boneAnimation.scaleZQueue().poll();

                DirtyTracker dirtyTracker = modelTracker.get(bone.getName());
                if (dirtyTracker == null) {
                    continue;
                }

                // 如果此骨骼有任何旋转值
                if (rXPoint != null && rYPoint != null && rZPoint != null) {
                    float valueX = MathUtil.lerpValues(rXPoint, controller.easingType, controller.customEasingMethod);
                    float valueY = MathUtil.lerpValues(rYPoint, controller.easingType, controller.customEasingMethod);
                    float valueZ = MathUtil.lerpValues(rZPoint, controller.easingType, controller.customEasingMethod);
                    pointData.rotationValueX += valueX;
                    pointData.rotationValueY += valueY;
                    pointData.rotationValueZ += valueZ;
                    if (controller.isAdditive()) {
                        bone.setRotationX(pointData.applied[0] + valueX + initialSnapshot.rotationValueX);
                        bone.setRotationY(pointData.applied[1] + valueY + initialSnapshot.rotationValueY);
                        bone.setRotationZ(pointData.applied[2] + valueZ + initialSnapshot.rotationValueZ);
                    } else if (controller.getName().startsWith("parallel_")) {
                        bone.setRotationX(pointData.rotationValueX + initialSnapshot.rotationValueX);
                        bone.setRotationY(pointData.rotationValueY + initialSnapshot.rotationValueY);
                        bone.setRotationZ(pointData.rotationValueZ + initialSnapshot.rotationValueZ);
                    } else {
                        bone.setRotationX(valueX + initialSnapshot.rotationValueX);
                        bone.setRotationY(valueY + initialSnapshot.rotationValueY);
                        bone.setRotationZ(valueZ + initialSnapshot.rotationValueZ);
                    }
                    pointData.applied[0] = bone.getRotationX() - initialSnapshot.rotationValueX;
                    pointData.applied[1] = bone.getRotationY() - initialSnapshot.rotationValueY;
                    pointData.applied[2] = bone.getRotationZ() - initialSnapshot.rotationValueZ;
                    snapshot.rotationValueX = bone.getRotationX();
                    snapshot.rotationValueY = bone.getRotationY();
                    snapshot.rotationValueZ = bone.getRotationZ();
                    snapshot.isCurrentlyRunningRotationAnimation = true;
                    dirtyTracker.hasRotationChanged = true;
                }

                // 如果此骨骼有任何位置值
                if (pXPoint != null && pYPoint != null && pZPoint != null) {
                    bone.setPositionX(
                            MathUtil.lerpValues(pXPoint, controller.easingType, controller.customEasingMethod) + (controller.isAdditive() ? pointData.applied[3] : 0));
                    bone.setPositionY(
                            MathUtil.lerpValues(pYPoint, controller.easingType, controller.customEasingMethod) + (controller.isAdditive() ? pointData.applied[4] : 0));
                    bone.setPositionZ(
                            MathUtil.lerpValues(pZPoint, controller.easingType, controller.customEasingMethod) + (controller.isAdditive() ? pointData.applied[5] : 0));
                    pointData.applied[3] = bone.getPositionX();
                    pointData.applied[4] = bone.getPositionY();
                    pointData.applied[5] = bone.getPositionZ();
                    snapshot.positionOffsetX = bone.getPositionX();
                    snapshot.positionOffsetY = bone.getPositionY();
                    snapshot.positionOffsetZ = bone.getPositionZ();
                    snapshot.isCurrentlyRunningPositionAnimation = true;
                    dirtyTracker.hasPositionChanged = true;
                }

                // 如果此骨骼有任何缩放点
                if (sXPoint != null && sYPoint != null && sZPoint != null) {
                    boolean parallel = controller.isAdditive() || controller.getName().startsWith("parallel_");
                    float[] values = {
                            MathUtil.lerpValues(sXPoint, controller.easingType, controller.customEasingMethod),
                            MathUtil.lerpValues(sYPoint, controller.easingType, controller.customEasingMethod),
                            MathUtil.lerpValues(sZPoint, controller.easingType, controller.customEasingMethod)
                    };
                    for (int axis = 0; axis < 3; axis++) {
                        if (parallel) pointData.scaleParallel[axis] *= values[axis];
                        else pointData.scaleBase[axis] = values[axis];
                        pointData.applied[6 + axis] = pointData.scaleBase[axis] * pointData.scaleParallel[axis];
                    }
                    bone.setScaleX(pointData.applied[6]);
                    bone.setScaleY(pointData.applied[7]);
                    bone.setScaleZ(pointData.applied[8]);
                    pointData.applied[6] = bone.getScaleX();
                    pointData.applied[7] = bone.getScaleY();
                    pointData.applied[8] = bone.getScaleZ();
                    snapshot.scaleValueX = bone.getScaleX();
                    snapshot.scaleValueY = bone.getScaleY();
                    snapshot.scaleValueZ = bone.getScaleZ();
                    snapshot.isCurrentlyRunningScaleAnimation = true;
                    dirtyTracker.hasScaleChanged = true;
                }
            }
        }
        this.reloadAnimations = false;

        double resetTickLength = manager.getResetSpeed();
        for (Map.Entry<String, DirtyTracker> tracker : modelTracker.entrySet()) {
            IBone model = tracker.getValue().model;
            BoneSnapshot initialSnapshot = model.getInitialSnapshot();
            BoneSnapshot saveSnapshot = boneSnapshots.get(tracker.getKey()).getRight();
            if (saveSnapshot == null) {
                if (crashWhenCantFindBone) {
                    throw new RuntimeException("Could not find save snapshot for bone: " + tracker.getValue().model.getName()
                            + ". Please don't add bones that are used in an animation at runtime.");
                } else {
                    continue;
                }
            }

            if (!tracker.getValue().hasRotationChanged) {
                if (saveSnapshot.isCurrentlyRunningRotationAnimation) {
                    // FIXME: 2023/7/12 莫名其妙修好了旋转 bug，原因未知
                    saveSnapshot.mostRecentResetRotationTick = 0;
                    saveSnapshot.isCurrentlyRunningRotationAnimation = false;
                }
                double percentageReset = Math.min((seekTime - saveSnapshot.mostRecentResetRotationTick) / resetTickLength, 1);
                model.setRotationX(MathUtil.lerpValues(percentageReset, saveSnapshot.rotationValueX,
                        initialSnapshot.rotationValueX));
                model.setRotationY(MathUtil.lerpValues(percentageReset, saveSnapshot.rotationValueY,
                        initialSnapshot.rotationValueY));
                model.setRotationZ(MathUtil.lerpValues(percentageReset, saveSnapshot.rotationValueZ,
                        initialSnapshot.rotationValueZ));
                if (percentageReset >= 1) {
                    saveSnapshot.rotationValueX = model.getRotationX();
                    saveSnapshot.rotationValueY = model.getRotationY();
                    saveSnapshot.rotationValueZ = model.getRotationZ();
                }
            }
            if (!tracker.getValue().hasPositionChanged) {
                if (saveSnapshot.isCurrentlyRunningPositionAnimation) {
                    saveSnapshot.mostRecentResetPositionTick = (float) seekTime;
                    saveSnapshot.isCurrentlyRunningPositionAnimation = false;
                }
                double percentageReset = Math.min((seekTime - saveSnapshot.mostRecentResetPositionTick) / resetTickLength, 1);
                model.setPositionX(MathUtil.lerpValues(percentageReset, saveSnapshot.positionOffsetX,
                        initialSnapshot.positionOffsetX));
                model.setPositionY(MathUtil.lerpValues(percentageReset, saveSnapshot.positionOffsetY,
                        initialSnapshot.positionOffsetY));
                model.setPositionZ(MathUtil.lerpValues(percentageReset, saveSnapshot.positionOffsetZ,
                        initialSnapshot.positionOffsetZ));
                if (percentageReset >= 1) {
                    saveSnapshot.positionOffsetX = model.getPositionX();
                    saveSnapshot.positionOffsetY = model.getPositionY();
                    saveSnapshot.positionOffsetZ = model.getPositionZ();
                }
            }
            if (!tracker.getValue().hasScaleChanged) {
                if (saveSnapshot.isCurrentlyRunningScaleAnimation) {
                    saveSnapshot.mostRecentResetScaleTick = (float) seekTime;
                    saveSnapshot.isCurrentlyRunningScaleAnimation = false;
                }
                double percentageReset = Math.min((seekTime - saveSnapshot.mostRecentResetScaleTick) / resetTickLength, 1);
                model.setScaleX(
                        MathUtil.lerpValues(percentageReset, saveSnapshot.scaleValueX, initialSnapshot.scaleValueX));
                model.setScaleY(
                        MathUtil.lerpValues(percentageReset, saveSnapshot.scaleValueY, initialSnapshot.scaleValueY));
                model.setScaleZ(
                        MathUtil.lerpValues(percentageReset, saveSnapshot.scaleValueZ, initialSnapshot.scaleValueZ));
                if (percentageReset >= 1) {
                    saveSnapshot.scaleValueX = model.getScaleX();
                    saveSnapshot.scaleValueY = model.getScaleY();
                    saveSnapshot.scaleValueZ = model.getScaleZ();
                }
            }
        }
        manager.isFirstTick = false;
        pose.capture(this.modelRendererList, seekTime);
    }

    private static final class CachedPose {
        final Map<String, Pair<IBone, BoneSnapshot>> bones = new HashMap<>();
        double tick = Double.NaN;

        CachedPose(List<IBone> renderers) {
            for (IBone bone : renderers) bones.put(bone.getName(), Pair.of(bone, new BoneSnapshot(bone.getInitialSnapshot())));
        }
        boolean matches(List<IBone> renderers) {
            if (bones.size() != renderers.size()) return false;
            for (IBone bone : renderers) {
                Pair<IBone, BoneSnapshot> saved = bones.get(bone.getName());
                if (saved == null || saved.getLeft() != bone) return false;
            }
            return true;
        }
        void restore(List<IBone> renderers) {
            for (IBone bone : renderers) {
                BoneSnapshot saved = bones.get(bone.getName()).getRight();
                bone.setRotationX(saved.rotationValueX); bone.setRotationY(saved.rotationValueY); bone.setRotationZ(saved.rotationValueZ);
                bone.setPositionX(saved.positionOffsetX); bone.setPositionY(saved.positionOffsetY); bone.setPositionZ(saved.positionOffsetZ);
                bone.setScaleX(saved.scaleValueX); bone.setScaleY(saved.scaleValueY); bone.setScaleZ(saved.scaleValueZ);
            }
        }
        void capture(List<IBone> renderers, double time) {
            for (IBone bone : renderers) {
                BoneSnapshot saved = bones.get(bone.getName()).getRight();
                saved.rotationValueX = bone.getRotationX(); saved.rotationValueY = bone.getRotationY(); saved.rotationValueZ = bone.getRotationZ();
                saved.positionOffsetX = bone.getPositionX(); saved.positionOffsetY = bone.getPositionY(); saved.positionOffsetZ = bone.getPositionZ();
                saved.scaleValueX = bone.getScaleX(); saved.scaleValueY = bone.getScaleY(); saved.scaleValueZ = bone.getScaleZ();
            }
            tick = time;
        }
    }

    private Map<String, DirtyTracker> createNewDirtyTracker() {
        Map<String, DirtyTracker> tracker = new Object2ObjectOpenHashMap<>();
        for (IBone bone : this.modelRendererList) {
            tracker.put(bone.getName(), new DirtyTracker(false, false, false, bone));
        }
        return tracker;
    }

    private void updateBoneSnapshots(Map<String, Pair<IBone, BoneSnapshot>> boneSnapshotCollection) {
        for (IBone bone : this.modelRendererList) {
            if (!boneSnapshotCollection.containsKey(bone.getName())) {
                boneSnapshotCollection.put(bone.getName(), Pair.of(bone, new BoneSnapshot(bone.getInitialSnapshot())));
            }
        }
    }

    public IBone getBone(String boneName) {
        for (IBone bone : this.modelRendererList) {
            if (bone.getName().equals(boneName)) {
                return bone;
            }
        }
        return null;
    }

    public void registerModelRenderer(IBone modelRenderer) {
        modelRenderer.saveInitialSnapshot();
        this.modelRendererList.add(modelRenderer);
    }

    public void clearModelRendererList() {
        this.modelRendererList.clear();
    }

    public List<IBone> getModelRendererList() {
        return this.modelRendererList;
    }

    public void preAnimationSetup(IAnimatable animatable, double seekTime) {
        this.animatedModel.setMolangQueries(animatable, seekTime);
    }
}
