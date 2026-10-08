package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = YesSteveModel.MOD_ID)
public class PlayerMoveEvent {
    @SubscribeEvent
    public static void onKeyboardInput(InputEvent.KeyInputEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (isMoveKey() && player != null) {
            CapabilityEvent.getModelInfoCap(player).ifPresent(cap -> {
                if (cap.isPlayAnimation()) {
                    com.elfmcys.yesstevemodel.client.ClientActions.playExtra(-1);
                }
            });
        }
    }

    private static boolean isMoveKey() {
        GameSettings options = Minecraft.getMinecraft().gameSettings;
        return options.keyBindForward.isPressed() || options.keyBindBack.isPressed() || options.keyBindLeft.isPressed() || options.keyBindRight.isPressed()
                || options.keyBindJump.isPressed() || options.keyBindSneak.isPressed();
    }
}
