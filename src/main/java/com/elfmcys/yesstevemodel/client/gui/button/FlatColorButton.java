package com.elfmcys.yesstevemodel.client.gui.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import com.elfmcys.yesstevemodel.client.gui.UiTheme;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class FlatColorButton extends Button {
    private boolean isSelect = false;
    private @Nullable List<String> tooltips;

    public FlatColorButton(int pX, int pY, int pWidth, int pHeight, String pMessage, Consumer<Button> pOnPress) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress);
    }

    public FlatColorButton setTooltips(String key) {
        this.tooltips = Collections.singletonList(I18n.format(key));
        return this;
    }

    public FlatColorButton setTooltips(@Nullable List<String> tooltips) {
        this.tooltips = tooltips;
        return this;
    }

    public void renderToolTip(GuiScreen screen, int pMouseX, int pMouseY) {
        if (this.hovered && this.tooltips != null) {
            screen.drawHoveringText(this.tooltips, pMouseX, pMouseY);
        }
    }

    @Override
    protected void renderWidget(@Nonnull Minecraft mc, int mouseX, int mouseY, float pPartialTick) {
        FontRenderer font = mc.fontRenderer;
        UiTheme.panel(x, y, width, height, isSelect ? UiTheme.SELECTED : hovered && enabled ? 0xFF263A4C : UiTheme.CARD, isSelect || hovered && enabled ? 0xFF559985 : UiTheme.BORDER);
        this.renderString(font, enabled ? (isSelect ? UiTheme.ACCENT : UiTheme.TEXT) : UiTheme.MUTED);
    }

    public void setSelect(boolean select) {
        this.isSelect = select;
    }
}
