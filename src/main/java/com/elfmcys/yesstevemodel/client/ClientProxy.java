package com.elfmcys.yesstevemodel.client;

import com.elfmcys.yesstevemodel.CommonProxy;
import com.elfmcys.yesstevemodel.client.animation.AnimationRegister;
import com.elfmcys.yesstevemodel.client.compat.*;
import com.elfmcys.yesstevemodel.client.input.*;
import com.elfmcys.yesstevemodel.client.renderer.CustomArrowRenderer;
import com.elfmcys.yesstevemodel.client.renderer.CustomPlayerRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {
    private static CustomPlayerRenderer CUSTOM_PLAYER_RENDERER;
    private static CustomArrowRenderer CUSTOM_ARROW_RENDERER;

    public static CustomPlayerRenderer getInstance() {
        return CUSTOM_PLAYER_RENDERER;
    }

    public static CustomArrowRenderer getArrowInstance() {
        return CUSTOM_ARROW_RENDERER;
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        CarryOnCompat.init();
        CrossbowCompat.init();
        ElytraCompat.init();
        RenderArmCompat.init();
        SpyglassCompat.init();
        SwimmingCompat.init();
        TridentCompat.init();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ClientRegistry.registerKeyBinding(PlayerModelScreenKey.PLAYER_MODEL_KEY);
        ClientRegistry.registerKeyBinding(AnimationRouletteKey.ANIMATION_ROULETTE_KEY);
        ClientRegistry.registerKeyBinding(DebugAnimationKey.DEBUG_ANIMATION_KEY);
        ClientRegistry.registerKeyBinding(ExtraPlayerConfigKey.EXTRA_PLAYER_RENDER_KEY);
        ExtraAnimationKey.registerKeyBinding();

        AnimationRegister.registerAnimationState();
        AnimationRegister.registerVariables();

        RenderManager context = Minecraft.getMinecraft().getRenderManager();
        CUSTOM_PLAYER_RENDERER = new CustomPlayerRenderer(context);
        CUSTOM_ARROW_RENDERER = new CustomArrowRenderer(context);
    }
}
