package com.elfmcys.yesstevemodel.client.compat;

import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.sirsquidly.oe.Main;
import com.sirsquidly.oe.capabilities.CapabilityRiptide;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.trident.Trident;
import net.minecraft.trident.util.EntityHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import thedarkcolour.futuremc.item.TridentItem;

import java.util.concurrent.atomic.AtomicBoolean;

public class TridentCompat {
    public static final String SPEAR_ACTION = "spear";

    private static final String OCEANIC_EXPANSE = "oe";
    private static boolean OE_INSTALLED = false;
    private static final String TRIDENT_MOD = "trident";
    private static boolean TM_INSTALLED = false;
    private static final String FMC = "futuremc";
    private static boolean FMC_INSTALLED = false;

    public static void init() {
        OE_INSTALLED = Loader.isModLoaded(OCEANIC_EXPANSE);
        TM_INSTALLED = Loader.isModLoaded(TRIDENT_MOD);
        FMC_INSTALLED = Loader.isModLoaded(FMC);
    }

    public static boolean isInstalled() {
        return OE_INSTALLED || TM_INSTALLED || FMC_INSTALLED;
    }

    public static boolean isAutoSpinAttack(EntityPlayer player) {
        return (OE_INSTALLED && isOEAttack(player)) ||
                (TM_INSTALLED && isTMAttack(player));
    }

    public static boolean isSpearAction(ItemStack stack) {
        return (OE_INSTALLED && isOESpear(stack)) ||
                (TM_INSTALLED && isTMSpear(stack)) ||
                (FMC_INSTALLED && isFMCSpear(stack));
    }

    @Optional.Method(modid = OCEANIC_EXPANSE)
    private static boolean isOEAttack(EntityPlayer player) {
        AtomicBoolean flag = new AtomicBoolean(false);
        CapabilityEvent.getCapability(player, CapabilityRiptide.RIPTIDE_CAP).ifPresent(cap -> {
            flag.set(cap.getRiptideAnimate());
        });
        return flag.get();
    }

    @Optional.Method(modid = TRIDENT_MOD)
    private static boolean isTMAttack(EntityPlayer player) {
        return EntityHelper.isSpinAttacking(player);
    }

    @Optional.Method(modid = OCEANIC_EXPANSE)
    private static boolean isOESpear(ItemStack stack) {
        return stack.getItemUseAction() == Main.SPEAR;
    }

    @Optional.Method(modid = TRIDENT_MOD)
    private static boolean isTMSpear(ItemStack stack) {
        return stack.getItemUseAction() == Trident.SPEAR;
    }

    @Optional.Method(modid = FMC)
    private static boolean isFMCSpear(ItemStack stack) {
        return stack.getItemUseAction() == TridentItem.getTRIDENT_USE_ACTION();
    }
}
