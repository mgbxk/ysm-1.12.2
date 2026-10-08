package com.elfmcys.yesstevemodel.mixin.early.client;

import com.elfmcys.yesstevemodel.client.compat.RenderArmCompat;
import com.elfmcys.yesstevemodel.client.event.ReplacePlayerHandRenderEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ForgeHooksClient.class, remap = false)
public class ForgeHooksClientMixin {
    @Inject(method = "renderSpecificFirstPersonHand", at = @At("HEAD"), cancellable = true)
    private static void cancelRenderHandEvent(
            EnumHand hand, float partialTicks, float interpPitch,
            float swingProgress, float equipProgress, ItemStack stack,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (RenderArmCompat.foundRenderArm()) return;
        if (ReplacePlayerHandRenderEvent.shouldRenderArm()) {
            cir.setReturnValue(false);
        }
    }
}
