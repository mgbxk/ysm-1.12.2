package com.elfmcys.yesstevemodel.geckolib3.model.provider;

import net.minecraft.util.ResourceLocation;

public interface IAnimatableModelProvider<E> {
    ResourceLocation getAnimationFileLocation(E animatable);
}
