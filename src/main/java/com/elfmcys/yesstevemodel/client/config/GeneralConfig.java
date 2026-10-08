package com.elfmcys.yesstevemodel.client.config;

import com.elfmcys.yesstevemodel.config.util.ConfigBuilder;

import javax.annotation.Nonnull;

public class GeneralConfig {
    public static boolean DISCLAIMER_SHOW = true;
    public static boolean PRINT_ANIMATION_ROULETTE_MSG = true;
    public static boolean DISABLE_SELF_MODEL = false;
    public static boolean DISABLE_OTHER_MODEL = false;
    public static boolean DISABLE_SELF_HANDS = false;
    public static boolean DISABLE_ARROWS_MODEL = false;
    public static boolean SHOW_MODEL_ID_FIRST = false;

    public static void build(@Nonnull ConfigBuilder builder) {
        builder.pushCategory("general");

        DISCLAIMER_SHOW = builder.get(
                "DisclaimerShow",
                DISCLAIMER_SHOW,
                "Whether to display disclaimer GUI"
        );

        PRINT_ANIMATION_ROULETTE_MSG = builder.get(
                "PrintAnimationRouletteMsg",
                PRINT_ANIMATION_ROULETTE_MSG,
                "Whether to print animation roulette play message"
        );

        DISABLE_SELF_MODEL = builder.get(
                "DisableSelfModel",
                DISABLE_SELF_MODEL,
                "Prevents rendering of self player's model"
        );

        DISABLE_OTHER_MODEL = builder.get(
                "DisableOtherModel",
                DISABLE_OTHER_MODEL,
                "Prevents rendering of other player's model"
        );

        DISABLE_SELF_HANDS = builder.get(
                "DisableSelfHands",
                DISABLE_SELF_HANDS,
                "Prevents rendering of self player's hand"
        );

        DISABLE_ARROWS_MODEL = builder.get(
                "DisableArrowsModel",
                DISABLE_ARROWS_MODEL,
                "Prevents rendering of arrows model"
        );

        SHOW_MODEL_ID_FIRST = builder.get(
                "ShowModelIdFirst",
                SHOW_MODEL_ID_FIRST,
                "Whether to display model ID first in the model selection screen, instead of the model name filled in by the model author."
        );

        builder.popCategory();
    }
}
