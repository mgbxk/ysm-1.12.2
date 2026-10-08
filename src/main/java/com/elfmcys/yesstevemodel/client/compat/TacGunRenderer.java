//package com.elfmcys.yesstevemodel.client.compat;
//
//import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
//import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
//import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
//import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationBuilder;
//import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
//import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
//import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone;
//import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
//import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
//import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils;
//import com.mojang.blaze3d.matrix.MatrixStack;
//import com.tac.guns.client.handler.AimingHandler;
//import com.tac.guns.client.handler.GunRenderingHandler;
//import com.tac.guns.client.handler.RecoilHandler;
//import com.tac.guns.client.handler.ReloadHandler;
//import com.tac.guns.common.Gun;
//import com.tac.guns.common.WeaponType;
//import com.tac.guns.item.GrenadeItem;
//import com.tac.guns.item.GunItem;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.entity.player.ClientPlayerEntity;
//import net.minecraft.client.renderer.IRenderTypeBuffer;
//import net.minecraft.client.renderer.model.ItemCameraTransforms;
//import net.minecraft.entity.LivingEntity;
//import net.minecraft.entity.Pose;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.Hand;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.math.vector.Vector3f;
//
//import javax.annotation.Nonnull;
//
//public class TacGunRenderer {
//    public static boolean isGun(ItemStack itemStack) {
//        return itemStack.getItem() instanceof GunItem;
//    }
//
//    public static boolean isGrenade(ItemStack itemStack) {
//        return itemStack.getItem() instanceof GrenadeItem;
//    }
//
//    public static void renderMainhandGun(ItemStack itemStack, LivingEntity player, MatrixStack poseStack, int packedLight, float partialTicks) {
//        poseStack.translate(0, -0.0625, -0.125);
//        poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
//        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
//        GunRenderingHandler.get().renderWeapon(player, itemStack, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, poseStack, buffer, packedLight, partialTicks);
//    }
//
//    public static void renderOffhandGun(ItemStack heldItem, GeoModel geoModel, LivingEntity player, MatrixStack poseStack, int packedLight, float partialTicks) {
//        Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
//        WeaponType weaponType = gun.getDisplay().getWeaponType();
//        if (weaponType == WeaponType.PT && !geoModel.tacPistolBones.isEmpty()) {
//            int size = geoModel.tacPistolBones.size();
//            for (int i = 0; i < size - 1; i++) {
//                RenderUtils.prepMatrixForBone(poseStack, geoModel.tacPistolBones.get(i));
//            }
//            GeoBone lastBone = geoModel.tacPistolBones.get(size - 1);
//            RenderUtils.translateMatrixToBone(poseStack, lastBone);
//            RenderUtils.translateToPivotPoint(poseStack, lastBone);
//            RenderUtils.rotateMatrixAroundBone(poseStack, lastBone);
//            RenderUtils.scaleMatrixForBone(poseStack, lastBone);
//
//            poseStack.translate(0, -0.125, 0);
//            poseStack.scale(0.65f, 0.65f, 0.65f);
//            poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
//            poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
//            IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
//            GunRenderingHandler.get().renderWeapon(player, heldItem, ItemCameraTransforms.TransformType.FIXED, poseStack, buffer, packedLight, partialTicks);
//        }
//        if (weaponType != WeaponType.PT && !geoModel.tacRifleBones.isEmpty()) {
//            int size = geoModel.tacRifleBones.size();
//            for (int i = 0; i < size - 1; i++) {
//                RenderUtils.prepMatrixForBone(poseStack, geoModel.tacRifleBones.get(i));
//            }
//            GeoBone lastBone = geoModel.tacRifleBones.get(size - 1);
//            RenderUtils.translateMatrixToBone(poseStack, lastBone);
//            RenderUtils.translateToPivotPoint(poseStack, lastBone);
//            RenderUtils.rotateMatrixAroundBone(poseStack, lastBone);
//            RenderUtils.scaleMatrixForBone(poseStack, lastBone);
//
//            poseStack.scale(0.65f, 0.65f, 0.65f);
//            poseStack.mulPose(Vector3f.YP.rotationDegrees(-180.0F));
//            IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
//            GunRenderingHandler.get().renderWeapon(player, heldItem, ItemCameraTransforms.TransformType.FIXED, poseStack, buffer, packedLight, partialTicks);
//        }
//    }
//
//    public static PlayState playGrenadeAnimation(AnimationEvent<CustomPlayerEntity> event, Hand hand) {
//        if (hand == Hand.MAIN_HAND) {
//            return playLoopAnimation(event, "tac:mainhand:grenade");
//        }
//        return playLoopAnimation(event, "tac:offhand:grenade");
//    }
//
//    /**
//     * tac:idle
//     * tac:run
//     * tac:walk
//     */
//    public static PlayState playGunMainAnimation(AnimationEvent<CustomPlayerEntity> event, String animationName, ILoopType loopType) {
//        String tacName = "tac:" + animationName;
//        ResourceLocation animation = event.getAnimatable().getAnimation();
//        if (GeckoLibCache.getInstance().getAnimations().get(animation).animations().containsKey(tacName)) {
//            return playAnimation(event, tacName, loopType);
//        }
//        return playAnimation(event, animationName, loopType);
//    }
//
//    /**
//     * tac:hold:pistol
//     * tac:aim:pistol
//     * tac:reload:pistol
//     * tac:aim_shoot:pistol
//     * tac:hold_shoot:pistol
//     * tac:run:pistol
//     */
//    public static PlayState playGunHoldAnimation(AnimationEvent<CustomPlayerEntity> event, ItemStack heldItem) {
//        Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
//        WeaponType weaponType = gun.getDisplay().getWeaponType();
//        PlayerEntity player = event.getAnimatable().getPlayer();
//        int fireTick = RecoilHandler.get().getRecoilTracker(player).getTick();
//
//        if (!player.isSwimming() && player.getPose() == Pose.SWIMMING) {
//            if (Math.abs(event.getLimbSwingAmount()) > 0.05) {
//                return getGunTypeAnimation(event, weaponType, "tac:climb:");
//            } else {
//                if (0 < fireTick && fireTick < 5) {
//                    return getGunTypeAnimation(event, weaponType, "tac:climbing:fire:");
//                }
//                return getGunTypeAnimation(event, weaponType, "tac:climbing:");
//            }
//        }
//
//        int reloadProgress = ReloadHandler.get().getReloadTimer();
//        ClientPlayerEntity localPlayer = Minecraft.getInstance().player;
//        if (reloadProgress > 0 && player.equals(localPlayer)) {
//            if (reloadProgress == 1) {
//                event.getController().shouldResetTick = true;
//                event.getController().adjustTick(0);
//            }
//            return getGunTypeAnimation(event, weaponType, "tac:reload:");
//        }
//
//        float aimProgress = AimingHandler.get().getAimProgress(player, event.getPartialTick());
//        if (aimProgress > 0) {
//            if (0 < fireTick && fireTick < 5) {
//                return getGunTypeAnimation(event, weaponType, "tac:aim:fire:");
//            }
//            return getGunTypeAnimation(event, weaponType, "tac:aim:");
//        } else {
//            if (player.isOnGround() && player.isSprinting()) {
//                return getGunTypeAnimation(event, weaponType, "tac:run:");
//            }
//            if (0 < fireTick && fireTick < 5) {
//                return getGunTypeAnimation(event, weaponType, "tac:hold:fire:");
//            }
//            return getGunTypeAnimation(event, weaponType, "tac:hold:");
//        }
//    }
//
//    @Nonnull
//    private static PlayState getGunTypeAnimation(AnimationEvent<CustomPlayerEntity> event, WeaponType weaponType, String prefix) {
//        switch (weaponType) {
//            case PT:
//                return playLoopAnimation(event, prefix + "pistol");
//            case RPG:
//                return playLoopAnimation(event, prefix + "rpg");
//            case AR:
//            case MG:
//            case SG:
//            case SR:
//            case SMG:
//            default:
//                return playLoopAnimation(event, prefix + "rifle");
//        }
//    }
//
//    @Nonnull
//    private static <P extends IAnimatable> PlayState playLoopAnimation(AnimationEvent<P> event, String animationName) {
//        return playAnimation(event, animationName, ILoopType.EDefaultLoopTypes.LOOP);
//    }
//
//    @Nonnull
//    private static <P extends IAnimatable> PlayState playAnimation(AnimationEvent<P> event, String animationName, ILoopType loopType) {
//        event.getController().setAnimation(new AnimationBuilder().addAnimation(animationName, loopType));
//        return PlayState.CONTINUE;
//    }
//}
