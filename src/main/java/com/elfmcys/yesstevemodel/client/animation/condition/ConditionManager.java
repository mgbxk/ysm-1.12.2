package com.elfmcys.yesstevemodel.client.animation.condition;

import com.google.common.collect.Maps;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class ConditionManager {
    public static Map<ResourceLocation, ConditionalSwing> SWING = Maps.newHashMap();
    public static Map<ResourceLocation, ConditionalSwing> SWING_OFFHAND = Maps.newHashMap();
    public static Map<ResourceLocation, ConditionalUse> USE_MAINHAND = Maps.newHashMap();
    public static Map<ResourceLocation, ConditionalUse> USE_OFFHAND = Maps.newHashMap();
    public static Map<ResourceLocation, ConditionalHold> HOLD_MAINHAND = Maps.newHashMap();
    public static Map<ResourceLocation, ConditionalHold> HOLD_OFFHAND = Maps.newHashMap();
    public static Map<ResourceLocation, ConditionArmor> ARMOR = Maps.newHashMap();

    public static void addTest(ResourceLocation id, String name) {
        SWING.computeIfAbsent(id, k -> new ConditionalSwing(EnumHand.MAIN_HAND)).addTest(name);
        SWING_OFFHAND.computeIfAbsent(id, k -> new ConditionalSwing(EnumHand.OFF_HAND)).addTest(name);
        USE_MAINHAND.computeIfAbsent(id, k -> new ConditionalUse(EnumHand.MAIN_HAND)).addTest(name);
        USE_OFFHAND.computeIfAbsent(id, k -> new ConditionalUse(EnumHand.OFF_HAND)).addTest(name);
        HOLD_MAINHAND.computeIfAbsent(id, k -> new ConditionalHold(EnumHand.MAIN_HAND)).addTest(name);
        HOLD_OFFHAND.computeIfAbsent(id, k -> new ConditionalHold(EnumHand.OFF_HAND)).addTest(name);
        ARMOR.computeIfAbsent(id, k -> new ConditionArmor()).addTest(name);
    }

    public static void clear() {
        SWING.clear();
        SWING_OFFHAND.clear();
        USE_MAINHAND.clear();
        USE_OFFHAND.clear();
        HOLD_MAINHAND.clear();
        HOLD_OFFHAND.clear();
        ARMOR.clear();
    }

    public static ConditionalSwing getSwingMainhand(ResourceLocation id) {
        return SWING.get(id);
    }

    public static ConditionalSwing getSwingOffhand(ResourceLocation id) {
        return SWING_OFFHAND.get(id);
    }

    public static ConditionalUse getUseMainhand(ResourceLocation id) {
        return USE_MAINHAND.get(id);
    }

    public static ConditionalUse getUseOffhand(ResourceLocation id) {
        return USE_OFFHAND.get(id);
    }

    public static ConditionalHold getHoldMainhand(ResourceLocation id) {
        return HOLD_MAINHAND.get(id);
    }

    public static ConditionalHold getHoldOffhand(ResourceLocation id) {
        return HOLD_OFFHAND.get(id);
    }

    public static ConditionArmor getArmor(ResourceLocation id) {
        return ARMOR.get(id);
    }
}
