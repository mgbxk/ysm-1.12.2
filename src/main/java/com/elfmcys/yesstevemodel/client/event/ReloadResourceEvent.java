package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.network.message.RequestLoadModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = YesSteveModel.MOD_ID)
public class ReloadResourceEvent {
    public static int DEBUG_BG_WIDTH = 1000;
    private static final Pattern INT_REG = Pattern.compile("^[0-9]*$");

    @SubscribeEvent
    public static void onTextureStitchEventPost(TextureStitchEvent.Post event) {
        if (event.getMap() == Minecraft.getMinecraft().getTextureMapBlocks()) {
            ClientModelManager.loadDefaultModel();
            if (com.elfmcys.yesstevemodel.client.ClientSession.isLocal() && Minecraft.getMinecraft().player != null)
                com.elfmcys.yesstevemodel.client.ClientSession.reloadLocalModels();
            else ClientModelManager.CACHE_MD5.forEach(RequestLoadModel::loadModel);
            Matcher matcher = INT_REG.matcher(I18n.format("molang.yes_steve_model.bg_width"));
            if (matcher.matches()) {
                DEBUG_BG_WIDTH = Integer.parseInt(I18n.format("molang.yes_steve_model.bg_width"));
            }
        }
    }
}
