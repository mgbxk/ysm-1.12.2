package com.elfmcys.yesstevemodel.client.model;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.animation.AnimationRegister;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone;
import com.elfmcys.yesstevemodel.geckolib3.model.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("all")
public class CustomPlayerModel extends AnimatedGeoModel {
    public static final ResourceLocation DEFAULT_MAIN_MODEL = ModelIdUtil.getMainId(new ResourceLocation(YesSteveModel.MOD_ID, "default"));
    public static final ResourceLocation DEFAULT_MAIN_ANIMATION = ModelIdUtil.getMainId(new ResourceLocation(YesSteveModel.MOD_ID, "default"));
    public static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(YesSteveModel.MOD_ID, "default/default.png");
    public static float FIRST_PERSON_HEAD_POS;

    @Override
    public ResourceLocation getModelLocation(Object object) {
        if (object instanceof CustomPlayerEntity customPlayer) {
            return customPlayer.getMainModel();
        }
        return DEFAULT_MAIN_MODEL;
    }

    // TODO：处理材质问题
    @Override
    public ResourceLocation getTextureLocation(Object object) {
        if (object instanceof CustomPlayerEntity customPlayer) {
            return customPlayer.getTexture();
        }
        return DEFAULT_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(Object object) {
        if (object instanceof CustomPlayerEntity customPlayer) {
            return customPlayer.getAnimation();
        }
        return DEFAULT_MAIN_ANIMATION;
    }

    @Override
    public void setCustomAnimations(IAnimatable animatable, int instanceId, AnimationEvent animationEvent) {
        List extraData = animationEvent.getExtraData();
        MolangParser parser = GeckoLibCache.getInstance().parser;
        if (com.elfmcys.yesstevemodel.util.RenderUtil.isPlayerPreviewRender()
                && this.getAnimationProcessor().restoreCachedPose(animatable, instanceId)) {
            if (extraData.size() == 1 && extraData.get(0) instanceof EntityModelData data
                    && animatable instanceof CustomPlayerEntity target && target.getPlayer() != null) {
                this.codeAnimation(animationEvent, data, target.getPlayer());
            }
            return;
        }
        if (!Minecraft.getMinecraft().isGamePaused() && extraData.size() == 1 && extraData.get(0) instanceof EntityModelData data
                && animatable instanceof CustomPlayerEntity customPlayer && customPlayer.getPlayer() != null) {
            EntityPlayer player = customPlayer.getPlayer();
            AnimationRegister.setParserValue(animationEvent, parser, data, player);
            super.setCustomAnimations(animatable, instanceId, animationEvent);
            this.codeAnimation(animationEvent, data, player);
        } else {
            super.setCustomAnimations(animatable, instanceId, animationEvent);
        }
    }

    @Deprecated
    private void codeAnimation(AnimationEvent animationEvent, EntityModelData data, EntityPlayer player) {
        // FIXME: 2023/6/21 这一块设计应该改成 molang 的，而且这个寻找效率低下
        IBone head = getBone("Head");
        FIRST_PERSON_HEAD_POS = 24;
        if (head != null) {
            head.setRotationX(head.getRotationX() + (float) Math.toRadians(data.headPitch));
            head.setRotationY(head.getRotationY() + (float) Math.toRadians(data.netHeadYaw));
            FIRST_PERSON_HEAD_POS = head.getPivotY() * ((CustomPlayerEntity) animationEvent.getAnimatable()).getHeightScale();
        }
        if (getCurrentModel().firstPersonViewLocator != null) {
            float heightScale = ((CustomPlayerEntity) animationEvent.getAnimatable()).getHeightScale();
            GeoBone locator = getCurrentModel().firstPersonViewLocator;
            FIRST_PERSON_HEAD_POS = locator.getPivotY() * heightScale;
        }
//        if (FirstPersonCompat.isInstalled() && getCurrentModel().firstPersonHead != null) {
//            FirstPersonCompat.hideHead(getCurrentModel().firstPersonHead);
//        }
    }

    @Override
    @Nullable
    public IBone getBone(String boneName) {
        return getAnimationProcessor().getBone(boneName);
    }

    @Override
    public void setMolangQueries(IAnimatable animatable, double seekTime) {
    }
}
