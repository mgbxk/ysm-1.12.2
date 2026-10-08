package com.elfmcys.yesstevemodel.client.compat;

import com.elfmcys.yesstevemodel.client.event.ReplacePlayerHandRenderEvent;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderArmCompat {
    private static final String CLEANROOM = "cleanroom";
    private static boolean RENDER_ARM_FOUND = false;

    public static void init() {
        if (Loader.isModLoaded(CLEANROOM)) {
            findRenderArm();
            if (RENDER_ARM_FOUND) {
                MinecraftForge.EVENT_BUS.register(CrlReplacePlayerHandRenderEvent.class);
            }
        }
    }

    private static void findRenderArm() {
        try {
            Class.forName("net.minecraftforge.client.event.RenderArmEvent");
            RENDER_ARM_FOUND = true;
        } catch (Exception ignored) {
        }
    }

    public static boolean foundRenderArm() {
        return RENDER_ARM_FOUND;
    }

    public static class CrlReplacePlayerHandRenderEvent {
        @SubscribeEvent
        public static void onRenderHand(RenderArmEvent event) {
            if (!ReplacePlayerHandRenderEvent.shouldRenderArm()) {
                return;
            }
            event.setCanceled(true);
            ReplacePlayerHandRenderEvent.renderArm(event.getArm());
        }
    }
}
