package com.elfmcys.yesstevemodel.client.model;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.animation.AnimationRegister;
import com.elfmcys.yesstevemodel.client.entity.CustomArrowEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser;
import com.elfmcys.yesstevemodel.geckolib3.model.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import net.minecraft.util.ResourceLocation;

@SuppressWarnings("rawtypes")
public class CustomArrowModel extends AnimatedGeoModel {
    public static final ResourceLocation DEFAULT_MAIN_MODEL = ModelIdUtil.getArrowId(new ResourceLocation(YesSteveModel.MOD_ID, "default"));
    public static final ResourceLocation DEFAULT_MAIN_ANIMATION = ModelIdUtil.getArrowId(new ResourceLocation(YesSteveModel.MOD_ID, "default"));
    public static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(YesSteveModel.MOD_ID, "default/arrow.png");

    @Override
    public void setMolangQueries(IAnimatable animatable, double seekTime) {
        MolangParser parser = GeckoLibCache.getInstance().parser;
        if (animatable instanceof CustomArrowEntity customArrow && customArrow.getArrow() != null) {
            AnimationRegister.setArrowParserValue(customArrow.getArrow(), parser);
        }
    }

    @Override
    public ResourceLocation getModelLocation(Object object) {
        if (object instanceof CustomArrowEntity arrowEntity) {
            return arrowEntity.getMainModel();
        }
        return DEFAULT_MAIN_MODEL;
    }

    @Override
    public ResourceLocation getTextureLocation(Object object) {
        if (object instanceof CustomArrowEntity arrowEntity) {
            return arrowEntity.getTexture();
        }
        return DEFAULT_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(Object object) {
        if (object instanceof CustomArrowEntity arrowEntity) {
            return arrowEntity.getAnimation();
        }
        return DEFAULT_MAIN_ANIMATION;
    }
}
