package com.elfmcys.yesstevemodel.client.compat;

import mod.acgaming.universaltweaks.config.UTConfigTweaks;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

/**
 * {@link com.elfmcys.yesstevemodel.util.EntityUtil#getCameraPosition(Minecraft, float)} 的兼容
 */
public class CameraCompat {
    private static final String UNIVERSAL_TWEAKS = "universaltweaks";
    private static boolean UT_INSTALLED = false;

    public static void init() {
        UT_INSTALLED = Loader.isModLoaded(UNIVERSAL_TWEAKS);
    }

    public static boolean bypassesNonSolidBlocks() {
        return UT_INSTALLED && utCamaraBypassesNonSolidBlocks();
    }

    @Optional.Method(modid = UNIVERSAL_TWEAKS)
    private static boolean utCamaraBypassesNonSolidBlocks() {
        return UTConfigTweaks.ENTITIES.utThirdPersonIgnoresNonSolidBlocks;
    }
}
