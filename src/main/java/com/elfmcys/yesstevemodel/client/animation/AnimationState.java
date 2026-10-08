package com.elfmcys.yesstevemodel.client.animation;

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.github.bsideup.jabel.Desugar;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

import java.util.function.BiPredicate;

@Desugar
public record AnimationState(
        String animationName,
        ILoopType loopType,
        int priority,
        BiPredicate<EntityPlayer, AnimationEvent<CustomPlayerEntity>> predicate
) {
    public AnimationState(String animationName, ILoopType loopType, int priority, BiPredicate<EntityPlayer, AnimationEvent<CustomPlayerEntity>> predicate) {
        this.animationName = animationName;
        this.loopType = loopType;
        this.priority = MathHelper.clamp(priority, Priority.HIGHEST, Priority.LOWEST);
        this.predicate = predicate;
    }
}
