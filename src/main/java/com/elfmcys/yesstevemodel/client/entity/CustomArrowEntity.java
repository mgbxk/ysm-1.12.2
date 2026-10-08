package com.elfmcys.yesstevemodel.client.entity;

import com.elfmcys.yesstevemodel.api.IArrowExtraInfo;
import com.elfmcys.yesstevemodel.client.model.CustomArrowModel;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationBuilder;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.manager.AnimationData;
import com.elfmcys.yesstevemodel.geckolib3.core.manager.AnimationFactory;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.geckolib3.util.GeckoLibUtil;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

import static com.elfmcys.yesstevemodel.api.IArrowExtraInfo.TEXTURE_NAME;

public class CustomArrowEntity implements IAnimatable {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this, true);
    private ResourceLocation mainModel = CustomArrowModel.DEFAULT_MAIN_MODEL;
    private ResourceLocation texture = CustomArrowModel.DEFAULT_TEXTURE;
    private EntityArrow arrow = null;

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "main", 2, this::predicateMain));
        for (int i = 0; i < 8; i++) {
            String controllerName = String.format("parallel_%d_controller", i);
            String animationName = String.format("parallel%d", i);
            data.addAnimationController(new AnimationController<>(this, controllerName, 0, e -> this.predicateParallel(e, animationName)));
        }
    }

    public ResourceLocation getMainModel() {
        if (GeckoLibCache.getInstance().getGeoModels().containsKey(this.mainModel)) {
            return this.mainModel;
        }
        return CustomArrowModel.DEFAULT_MAIN_MODEL;
    }

    public ResourceLocation getAnimation() {
        if (GeckoLibCache.getInstance().getAnimations().containsKey(this.mainModel)) {
            return this.mainModel;
        }
        return CustomArrowModel.DEFAULT_MAIN_ANIMATION;
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    public EntityArrow getArrow() {
        return this.arrow;
    }

    public void setArrow(EntityArrow arrow) {
        this.arrow = arrow;
        if (arrow instanceof IArrowExtraInfo extraInfo) {
            this.mainModel = ModelIdUtil.getArrowId(new ResourceLocation(extraInfo.getYsmModelId()));
            this.texture = ModelIdUtil.getSubModelId(new ResourceLocation(extraInfo.getYsmModelId()), TEXTURE_NAME);
        }
    }

    public PlayState predicateMain(AnimationEvent<CustomArrowEntity> event) {
        EntityArrow arrowEntity = event.getAnimatable().getArrow();
        if (arrowEntity == null) {
            return PlayState.STOP;
        }
        if (arrowEntity.isInWater()) {
            return playAnimation(event, "water");
        }
        if (arrowEntity.isBurning()) {
            return playAnimation(event, "fire");
        }
        if (arrowEntity.inGround) {
            return playAnimation(event, "ground");
        } else {
            return playAnimation(event, "air");
        }
    }

    public PlayState predicateParallel(AnimationEvent<CustomArrowEntity> event, String animationName) {
        if (Minecraft.getMinecraft().isGamePaused()) {
            return PlayState.STOP;
        }
        event.getController().setAnimation(new AnimationBuilder().addAnimation(animationName, ILoopType.EDefaultLoopTypes.LOOP));
        return playAnimation(event, animationName);
    }

    @Nonnull
    private static <P extends IAnimatable> PlayState playAnimation(AnimationEvent<P> event, String animationName) {
        return PlayState.CONTINUE;
    }
}
