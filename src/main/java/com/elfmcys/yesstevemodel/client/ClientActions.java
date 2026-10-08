package com.elfmcys.yesstevemodel.client;

import com.elfmcys.yesstevemodel.client.animation.modern.ModernAssets;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.SetNamedAnimation;
import com.elfmcys.yesstevemodel.network.message.SetPlayAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/** All client UI traffic goes through the active connection mode. */
public final class ClientActions {
    private ClientActions() {}
    public static void sendToServer(IMessage message) {
        if (ClientSession.isSynced() && Minecraft.getMinecraft().getConnection() != null) NetworkHandler.CHANNEL.sendToServer(message);
    }
    public static void playExtra(int index) {
        if (index < -1 || index >= 8) return;
        if (ClientSession.isLocal()) CapabilityEvent.getModelInfoCap(Minecraft.getMinecraft().player).ifPresent(cap -> {
            if (index == -1) cap.stopAnimation(); else cap.playAnimation("extra" + index);
        });
        else sendToServer(index == -1 ? SetPlayAnimation.stop() : new SetPlayAnimation(index));
    }
    public static void playNamed(ResourceLocation model, String animation) {
        if (ClientSession.isLocal()) CapabilityEvent.getModelInfoCap(Minecraft.getMinecraft().player).ifPresent(cap -> {
            ModernAssets.Bundle bundle = ModernAssets.MODELS.get(com.elfmcys.yesstevemodel.util.ModelIdUtil.getMainId(model));
            if (cap.getModelId().equals(model) && bundle != null && bundle.options.playable.contains(animation)) cap.playAnimation(animation);
        });
        else sendToServer(new SetNamedAnimation(model.toString(), animation));
    }
}
