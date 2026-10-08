package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.gui.button.FlatColorButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

public class DownloadScreen extends Screen {
    private final PlayerModelScreen parent;
    private int x;
    private int y;

    public DownloadScreen(PlayerModelScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        this.x = (this.width - 420) / 2;
        this.y = (this.height - 235) / 2;

        this.addButton(new FlatColorButton(this.x + 5, this.y, 80, 18, I18n.format("gui.yes_steve_model.model.return"), (b) -> this.mc.displayGuiScreen(this.parent)));
    }

    @Override
    public void drawScreen(int pMouseX, int pMouseY, float pPartialTick) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, TextFormatting.DARK_RED + "Coming Soooooooooooooooooooooooooon™", this.width / 2, this.height / 2 - 5, 0xFFFFFFFF);
        super.drawScreen(pMouseX, pMouseY, pPartialTick);
    }
}
