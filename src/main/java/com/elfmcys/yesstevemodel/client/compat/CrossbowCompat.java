package com.elfmcys.yesstevemodel.client.compat;

import git.jbredwards.crossbow.api.ICrossbow;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowProjectiles;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.smileycorp.crossbows.common.item.ItemCrossbow;

public class CrossbowCompat {
    public static final String CROSSBOW_ACTION = "crossbow";

    private static final String J_CROSSBOW = "crossbow";
    private static boolean JCROSSBOW_INSTALLED = false;
    private static final String S_CROSSBOWS = "crossbows";
    private static boolean SCROSSBOWS_INSTALLED = false;

    public static void init() {
        JCROSSBOW_INSTALLED = Loader.isModLoaded(J_CROSSBOW);
        SCROSSBOWS_INSTALLED = Loader.isModLoaded(S_CROSSBOWS);
    }

    public static boolean isInstalled() {
        return JCROSSBOW_INSTALLED || SCROSSBOWS_INSTALLED;
    }

    public static boolean isCharged(ItemStack stack) {
        return (JCROSSBOW_INSTALLED && isJCrossbowCharged(stack)) ||
                (SCROSSBOWS_INSTALLED && isSCrossbowsCharged(stack));
    }

    public static boolean isCrossbowAction(ItemStack stack) {
        return (JCROSSBOW_INSTALLED && isJCrossbowAction(stack)) ||
                (SCROSSBOWS_INSTALLED && isSCrossbowsAction(stack));
    }

    @Optional.Method(modid = J_CROSSBOW)
    private static boolean isJCrossbowCharged(ItemStack stack) {
        if (stack.getItem() instanceof ICrossbow) {
            ICrossbowProjectiles cap = ICrossbowProjectiles.get(stack);
            return cap != null && !cap.isEmpty();
        }
        return false;
    }

    @Optional.Method(modid = S_CROSSBOWS)
    private static boolean isSCrossbowsCharged(ItemStack stack) {
        if (stack.getItem() instanceof ItemCrossbow) {
            return ItemCrossbow.isCharged(stack);
        }
        return false;
    }

    @Optional.Method(modid = J_CROSSBOW)
    private static boolean isJCrossbowAction(ItemStack stack) {
        return stack.getItemUseAction() == ICrossbow.CROSSBOW_ACTION;
    }

    @Optional.Method(modid = S_CROSSBOWS)
    private static boolean isSCrossbowsAction(ItemStack stack) {
        return stack.getItem() instanceof ItemCrossbow;
    }
}
