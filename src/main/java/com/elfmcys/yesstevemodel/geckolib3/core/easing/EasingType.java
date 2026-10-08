package com.elfmcys.yesstevemodel.geckolib3.core.easing;

import java.util.Locale;

/**
 * 插值类型
 */
public enum EasingType {
    NONE, CUSTOM, LINEAR, STEP;

    public static EasingType getEasingTypeFromString(String search) {
        return switch (search.toLowerCase(Locale.ROOT)) {
            case "custom" -> CUSTOM;
            case "linear" -> LINEAR;
            case "step" -> STEP;
            default -> NONE;
        };
    }
}
