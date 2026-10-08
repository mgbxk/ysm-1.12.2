package com.elfmcys.yesstevemodel.client.gui.button;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class ConfigCheckBox extends Checkbox {
    /**
     * @param font 计算宽度用，文本较长时按钮宽度（点击区域）也会跟着变长。
     */
    public ConfigCheckBox(int pX, int pY, String key, @Nonnull FontRenderer font, boolean current, Consumer<Boolean> setter) {
        super(pX, pY, I18n.format("gui.yes_steve_model." + key), font, current, (button -> {
            setter.accept(((Checkbox) button).selected());
        }));
    }
}
