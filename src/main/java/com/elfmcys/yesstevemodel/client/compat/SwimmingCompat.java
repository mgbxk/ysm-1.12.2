package com.elfmcys.yesstevemodel.client.compat;

import com.fuzs.aquaacrobatics.entity.Pose;
import com.fuzs.aquaacrobatics.entity.player.IPlayerResizeable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

public class SwimmingCompat {
    private static final String AQUA_ACROBATICS = "aquaacrobatics";
    private static boolean AA_INSTALLED = false;

    public static void init() {
        AA_INSTALLED = Loader.isModLoaded(AQUA_ACROBATICS);
    }

    public static boolean isInstalled() {
        return AA_INSTALLED;
    }

    public static boolean isSwimming(EntityPlayer player) {
        return AA_INSTALLED && isAASwimming(player);
    }

    public static boolean isSwimmingPose(EntityPlayer player) {
        return AA_INSTALLED && isAASwimmingPose(player);
    }

    @Optional.Method(modid = AQUA_ACROBATICS)
    private static boolean isAASwimming(EntityPlayer player) {
        if (player instanceof IPlayerResizeable resizeable) {
            return resizeable.isSwimming();
        }
        return false;
    }

    @Optional.Method(modid = AQUA_ACROBATICS)
    private static boolean isAASwimmingPose(EntityPlayer player) {
        if (player instanceof IPlayerResizeable resizeable) {
            return resizeable.getPose() == Pose.SWIMMING;
        }
        return false;
    }
}
