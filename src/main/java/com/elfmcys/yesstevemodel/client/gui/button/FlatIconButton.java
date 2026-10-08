package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.YesSteveModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class FlatIconButton extends FlatColorButton {
    private final static ResourceLocation ICON = new ResourceLocation(YesSteveModel.MOD_ID, "texture/icon.png");
    private final int textureX;
    private final int textureY;

    public FlatIconButton(int x, int y, int width, int height, int textureX, int textureY, Consumer<Button> onPress) {
        super(x, y, width, height, "", onPress);
        this.textureX = textureX;
        this.textureY = textureY;
    }

    @Override
    protected void renderWidget(@Nonnull Minecraft mc, int mouseX, int mouseY, float pPartialTick) {
        super.renderWidget(mc, mouseX, mouseY, pPartialTick);
        mc.getTextureManager().bindTexture(ICON);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableDepth();
        int startX = (this.width - 16) / 2;
        int startY = (this.height - 16) / 2;
        this.drawTexturedModalRect(this.x + startX, this.y + startY, this.textureX, this.textureY, 16, 16);
    }
}
