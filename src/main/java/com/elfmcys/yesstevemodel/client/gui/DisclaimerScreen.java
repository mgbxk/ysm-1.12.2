package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.config.GeneralConfig;
import com.elfmcys.yesstevemodel.client.gui.button.Button;
import com.elfmcys.yesstevemodel.client.gui.button.Checkbox;
import com.elfmcys.yesstevemodel.config.Config;
import net.minecraft.client.resources.I18n;

import java.util.List;

public class DisclaimerScreen extends Screen {
    private Checkbox readCheckbox;
    private int x;
    private int y;

    public DisclaimerScreen() {
    }

    @Override
    public void initGui() {
        String mainText = I18n.format("gui.yes_steve_model.disclaimer.text");
        List<String> splitMainText = this.listLineBreakStringToWidth(mainText, 400);
        int totalHeight = splitMainText.size() * this.fontRenderer.FONT_HEIGHT + 20 + 20 + 10 + 20;
        this.x = (this.width - 400) / 2;
        this.y = (this.height - totalHeight) / 2;

        String readCheckboxText = I18n.format("gui.yes_steve_model.disclaimer.read");
        int readTextWidth = this.fontRenderer.getStringWidth(readCheckboxText);
        this.readCheckbox = new Checkbox((this.width - readTextWidth) / 2, this.y + totalHeight - 50, readCheckboxText, this.fontRenderer, !GeneralConfig.DISCLAIMER_SHOW);
        this.addButton(this.readCheckbox);
        this.addButton(new Button((this.width - 300) / 2, this.y + totalHeight - 20, 300, 20, I18n.format("gui.yes_steve_model.disclaimer.close"), b -> {
            if (this.readCheckbox.selected()) {
                GeneralConfig.DISCLAIMER_SHOW = false;
                Config.save();
                this.mc.displayGuiScreen(new PlayerModelScreen());
            } else {
                this.mc.displayGuiScreen(null);
            }
        }));
    }

    @Override
    public void drawScreen(int pMouseX, int pMouseY, float pPartialTick) {
        this.drawDefaultBackground();
        this.drawWordWrap(I18n.format("gui.yes_steve_model.disclaimer.text"), this.x, this.y, 400, 0xffffffff);
        super.drawScreen(pMouseX, pMouseY, pPartialTick);
    }
}
