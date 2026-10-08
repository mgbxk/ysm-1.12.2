package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.client.gui.UiTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class UiButton extends GuiButton {
    public boolean selected;
    public UiButton(int id, int x, int y, int width, int height, String text) { super(id, x, y, width, height, text); }
    @Override public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        UiTheme.panel(x, y, width, height, selected ? UiTheme.SELECTED : hovered && enabled ? 0xFF263A4C : UiTheme.CARD, selected || hovered && enabled ? 0xFF559985 : UiTheme.BORDER);
        UiTheme.centered(mc.fontRenderer, UiTheme.fit(mc.fontRenderer, displayString, width - 10), x + width / 2, y + (height - mc.fontRenderer.FONT_HEIGHT) / 2, enabled ? (selected ? UiTheme.ACCENT : UiTheme.TEXT) : UiTheme.MUTED);
    }
}
