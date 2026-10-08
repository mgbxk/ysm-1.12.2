package com.elfmcys.yesstevemodel.client.config;

import com.elfmcys.yesstevemodel.Tags;
import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;

import java.util.Collections;
import java.util.Set;

@SuppressWarnings("unused")
public class ConfigGuiFactory implements IModGuiFactory {
    @Override
    public void initialize(Minecraft minecraftInstance) {
    }

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(final GuiScreen parentScreen) {
        return new ConfigGui(parentScreen);
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return Collections.emptySet();
    }

    /**
     * Forge 的 Config Gui，更直观一点。
     */
    public static class ConfigGui extends GuiConfig {
        public ConfigGui(final GuiScreen parent) {
            super(parent, Config.getRootConfigElements(), YesSteveModel.MOD_ID, false, false, Tags.MOD_NAME);
        }
    }
}
