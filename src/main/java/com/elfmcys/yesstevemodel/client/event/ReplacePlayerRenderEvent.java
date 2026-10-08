package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.ClientProxy;
import com.elfmcys.yesstevemodel.client.compat.CarryOnCompat;
import com.elfmcys.yesstevemodel.client.config.GeneralConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = YesSteveModel.MOD_ID)
public class ReplacePlayerRenderEvent {
    @SubscribeEvent
    public static void onRender(RenderPlayerEvent.Pre event) {
        EntityPlayer playerRender = event.getEntityPlayer();
        EntityPlayerSP playerSelf = Minecraft.getMinecraft().player;
        if (!com.elfmcys.yesstevemodel.client.ClientSession.shouldRender(playerRender)) return;
        if (playerRender.equals(playerSelf) && GeneralConfig.DISABLE_SELF_MODEL) {
            return;
        }
        if (!playerRender.equals(playerSelf) && GeneralConfig.DISABLE_OTHER_MODEL) {
            return;
        }
        event.setCanceled(true);
        ClientProxy.getInstance().doRender(playerRender, event.getX(), event.getY(), event.getZ(), playerRender.rotationYaw, event.getPartialRenderTick());
        CarryOnCompat.renderCarryOn(playerRender, event.getRenderer(), event.getPartialRenderTick(), event.getX(), event.getY(), event.getZ());
    }
}
