package com.elfmcys.yesstevemodel.mixin.early.client;

import com.elfmcys.yesstevemodel.client.compat.RenderArmCompat;
import com.elfmcys.yesstevemodel.client.event.ReplacePlayerHandRenderEvent;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPlayer.class)
public class RenderPlayerMixin {
    @Inject(method = "renderLeftArm", at = @At("HEAD"), cancellable = true)
    private void addRenderLeftArmEvent(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        if (RenderArmCompat.foundRenderArm()) return;
        if (ReplacePlayerHandRenderEvent.shouldRenderArm()) {
            ReplacePlayerHandRenderEvent.renderArm(EnumHandSide.LEFT);
            ci.cancel();
        }
    }

    @Inject(method = "renderRightArm", at = @At("HEAD"), cancellable = true)
    private void addRenderRightArmEvent(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        if (RenderArmCompat.foundRenderArm()) return;
        if (ReplacePlayerHandRenderEvent.shouldRenderArm()) {
            ReplacePlayerHandRenderEvent.renderArm(EnumHandSide.RIGHT);
            ci.cancel();
        }
    }
}
