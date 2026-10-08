package com.elfmcys.yesstevemodel.client.animation.condition;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;

public class ConditionalHold extends AbstractConditionItem {
    private static final String EMPTY_MAINHAND = "hold_mainhand:empty";
    private static final String EMPTY_OFFHAND = "hold_offhand:empty";

    public ConditionalHold(EnumHand hand) {
        super("hold_" + (hand == EnumHand.MAIN_HAND ? "mainhand" : "offhand"));
    }

    @Override
    public String doTest(EntityPlayer player, EnumHand hand) {
        if (player.getHeldItem(hand).isEmpty()) {
            return hand == EnumHand.MAIN_HAND ? EMPTY_MAINHAND : EMPTY_OFFHAND;
        }
        return super.doTest(player, hand);
    }
}
