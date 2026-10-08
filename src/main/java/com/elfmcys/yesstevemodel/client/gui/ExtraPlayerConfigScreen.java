package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.config.ExtraPlayerScreenConfig;
import com.elfmcys.yesstevemodel.client.input.ExtraPlayerConfigKey;
import com.elfmcys.yesstevemodel.config.Config;
import com.elfmcys.yesstevemodel.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

public class ExtraPlayerConfigScreen extends Screen {
    private int posX;
    private int posY;
    private float scale;
    private float yawOffset;
    private boolean isChangePos = false;
    private boolean isChangeScale = false;

    public ExtraPlayerConfigScreen() {
        this.posX = ExtraPlayerScreenConfig.PLAYER_POS_X;
        this.posY = ExtraPlayerScreenConfig.PLAYER_POS_Y;
        this.scale = ExtraPlayerScreenConfig.PLAYER_SCALE;
        this.yawOffset = ExtraPlayerScreenConfig.PLAYER_YAW_OFFSET;
    }

    @Override
    public void drawScreen(int pMouseX, int pMouseY, float pPartialTick) {
        int startX = this.posX;
        int startY = this.posY;
        int endX = (int) (startX + this.scale * 1);
        int endY = (int) (startY + this.scale * 2);

        this.drawVerticalLine(this.width / 2 - 1, -2, this.height + 2, 0x9fffffff);
        this.drawHorizontalLine(-2, this.width + 2, this.height / 2 - 1, 0x9fffffff);

        this.drawVerticalLine(10, -2, this.height + 2, 0x9fffffff);
        this.drawVerticalLine(this.width - 10, -2, this.height + 2, 0x9fffffff);
        this.drawHorizontalLine(-2, this.width + 2, 10, 0x9fffffff);
        this.drawHorizontalLine(-2, this.width + 2, this.height - 10, 0x9fffffff);

        this.drawVerticalLine(startX, startY, endY, 0xffff0000);
        this.drawVerticalLine(endX, startY, endY, 0xffff0000);
        this.drawHorizontalLine(startX, endX, startY, 0xffff0000);
        this.drawHorizontalLine(startX, endX, endY, 0xffff0000);

        this.drawGradientRect(startX, startY, endX, endY, 0x4fffffff, 0x4fffffff);

        this.drawGradientRect(startX - 5, startY - 5, startX + 5, startY + 5, 0xFF00FF9F, 0xFF00FF9F);
        this.drawGradientRect(endX - 5, endY - 5, endX + 5, endY + 5, 0xFF00009F, 0xFF00009F);

        int y = 15;
        String component = I18n.format("gui.yes_steve_model.extra_player_render.tips");
        List<String> split = this.listLineBreakStringToWidth(component, 500);
        for (String charSequence : split) {
            int w = this.fontRenderer.getStringWidth(charSequence);
            this.drawString(this.fontRenderer, charSequence, this.width - 15 - w, y, 0xFFFFFF);
            y += 10;
        }

        if (this.mc.player != null) {
            RenderUtil.renderPlayerEntity(this.mc.player, this.posX, this.posY, this.scale, this.yawOffset, 50);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        boolean xIn = this.posX - 5 < mouseX && mouseX < this.posX + 5;
        boolean yIn = this.posY - 5 < mouseY && mouseY < this.posY + 5;
        if (button == LEFT_MOUSE_BUTTON && xIn && yIn) {
            this.isChangePos = true;
        }
        int endX = (int) (this.posX + this.scale * 1);
        int endY = (int) (this.posY + this.scale * 2);
        boolean xIn2 = endX - 5 < mouseX && mouseX < endX + 5;
        boolean yIn2 = endY - 5 < mouseY && mouseY < endY + 5;
        if (button == LEFT_MOUSE_BUTTON && xIn2 && yIn2) {
            this.isChangeScale = true;
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        this.isChangePos = false;
        this.isChangeScale = false;
        super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseDragged(int mouseX, int mouseY, int button, int deltaX, int deltaY) {
        if (this.isChangeScale) {
            double scale1 = mouseX - this.posX;
            double scale2 = (double) (mouseY - this.posY) / 2;
            this.scale = (float) Math.min(scale1, scale2);
            return;
        }
        if (this.isChangePos) {
            this.posX = mouseX;
            this.posY = mouseY;
            return;
        }
        if (button == RIGHT_MOUSE_BUTTON) {
            this.yawOffset += (deltaX * 2);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_R && isAltKeyDown()) {
            this.posX = 10;
            this.posY = 10;
            this.scale = 40;
            this.yawOffset = 5;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        ExtraPlayerScreenConfig.PLAYER_POS_X = this.posX;
        ExtraPlayerScreenConfig.PLAYER_POS_Y = this.posY;
        ExtraPlayerScreenConfig.PLAYER_SCALE = this.scale;
        ExtraPlayerScreenConfig.PLAYER_YAW_OFFSET = this.yawOffset;
        Config.save();
        super.onGuiClosed();
    }

    @Override
    public void onResize(@Nonnull Minecraft mc, int width, int height) {
        ExtraPlayerScreenConfig.PLAYER_POS_X = this.posX;
        ExtraPlayerScreenConfig.PLAYER_POS_Y = this.posY;
        ExtraPlayerScreenConfig.PLAYER_SCALE = this.scale;
        ExtraPlayerScreenConfig.PLAYER_YAW_OFFSET = this.yawOffset;
        Config.save();
        super.onResize(mc, width, height);
    }

    @Override
    protected boolean canGuiClose(int keyCode) {
        return super.canGuiClose(keyCode) || isKeyActiveIgnoreConflict(ExtraPlayerConfigKey.EXTRA_PLAYER_RENDER_KEY, keyCode);
    }
}
