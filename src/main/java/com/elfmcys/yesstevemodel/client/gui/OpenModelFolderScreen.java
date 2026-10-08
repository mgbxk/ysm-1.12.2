package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.gui.button.Button;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import net.minecraft.client.resources.I18n;

import java.io.File;

public class OpenModelFolderScreen extends Screen {
    private final PlayerModelScreen screen;

    protected OpenModelFolderScreen(PlayerModelScreen screen) {
        this.screen = screen;
    }

    @Override
    public void initGui() {
        int x = (this.width - 310) / 2;
        int y = this.height / 2 + 60;
        this.addButton(new Button(x, y, 150, 20, I18n.format("gui.yes_steve_model.open_model_folder.open"), b -> {
            try {
                Class<?> oclass = Class.forName("java.awt.Desktop");
                Object object = oclass.getMethod("getDesktop").invoke(null);
                oclass.getMethod("open", File.class).invoke(object, ServerModelManager.CUSTOM.toFile());
            } catch (Exception e) {
                YesSteveModel.LOGGER.error("Couldn't open file", e);
            }
        }));
        this.addButton(new Button(x + 160, y, 150, 20, I18n.format("gui.yes_steve_model.model.return"), b -> {
            this.mc.displayGuiScreen(this.screen);
        }));
        if (com.elfmcys.yesstevemodel.client.ClientSession.isLocal()) {
            this.addButton(new Button(x, y + 30, 310, 20, "重新加载本地模型", b -> {
                com.elfmcys.yesstevemodel.client.ClientSession.reloadLocalModels();
                this.mc.displayGuiScreen(this.screen);
            }));
        }
    }

    @Override
    public void drawScreen(int pMouseX, int pMouseY, float pPartialTick) {
        this.drawDefaultBackground();
        this.drawWordWrap(I18n.format("gui.yes_steve_model.open_model_folder.tips"),
                (this.width - 400) / 2, this.height / 2 - 80, 400, 0XFFFFFF);
        super.drawScreen(pMouseX, pMouseY, pPartialTick);
    }
}
