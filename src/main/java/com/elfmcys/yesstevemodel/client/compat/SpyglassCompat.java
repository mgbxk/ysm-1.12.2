package com.elfmcys.yesstevemodel.client.compat;

import com.deeperdepths.common.items.ItemSpyglass;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

public class SpyglassCompat {
    public static final String SPYGLASS_ACTION = "spyglass";

    private static final String DEEPER_DEPTHS = "deeperdepths";
    private static boolean DD_INSTALLED = false;

    public static void init() {
        DD_INSTALLED = Loader.isModLoaded(DEEPER_DEPTHS);
    }

    public static boolean isInstalled() {
        return DD_INSTALLED;
    }

    public static boolean isSpyglassAction(ItemStack stack) {
        return (DD_INSTALLED && isDDAction(stack));
    }

    @Optional.Method(modid = DEEPER_DEPTHS)
    private static boolean isDDAction(ItemStack stack) {
        return stack.getItem() instanceof ItemSpyglass;
    }
}
