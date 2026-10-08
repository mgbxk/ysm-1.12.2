package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.network.message.RequestServerModelInfo;
import com.elfmcys.yesstevemodel.util.FileSizeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class ModelInfoButton extends Button {
    private final RequestServerModelInfo.Info info;
    private boolean isSelect = false;

    public ModelInfoButton(int pX, int pY, int pHeight, RequestServerModelInfo.Info info, Consumer<Button> pOnPress) {
        super(pX, pY, 250, pHeight, "", pOnPress);
        this.info = info;
    }

    @Override
    protected void renderWidget(@Nonnull Minecraft mc, int mouseX, int mouseY, float pPartialTick) {
        FontRenderer font = mc.fontRenderer;
        int color = this.isSelect ? 0xff_1E90FF : 0xff_434242;
        this.drawGradientRect(this.x, this.y, this.x + this.width, this.y + this.height, color, color);
        if (this.isMouseOver()) {
            this.drawGradientRect(this.x, this.y + 1, this.x + 1, this.y + this.height - 1, 0xff_F3EFE0, 0xff_F3EFE0);
            this.drawGradientRect(this.x, this.y, this.x + this.width, this.y + 1, 0xff_F3EFE0, 0xff_F3EFE0);
            this.drawGradientRect(this.x + this.width - 1, this.y + 1, this.x + this.width, this.y + this.height - 1, 0xff_F3EFE0, 0xff_F3EFE0);
            this.drawGradientRect(this.x, this.y + this.height - 1, this.x + this.width, this.y + this.height, 0xff_F3EFE0, 0xff_F3EFE0);
        }
        this.drawString(font, this.info.getFileName(), this.x + 5, this.y + (this.height - 8) / 2, 0xF3EFE0);
        final String text = switch (this.info.getType()) {
            case FOLDER -> TextFormatting.AQUA + I18n.format("gui.yes_steve_model.model_manage.type.folder");
            case ZIP -> TextFormatting.GOLD + I18n.format("gui.yes_steve_model.model_manage.type.zip");
//            case SEVEN_Z -> TextFormatting.LIGHT_PURPLE + I18n.format("gui.yes_steve_model.model_manage.type.seven_z");
            case YSM -> TextFormatting.YELLOW + I18n.format("gui.yes_steve_model.model_manage.type.ysm");
            case UNKNOWN -> TextFormatting.DARK_GRAY + I18n.format("gui.yes_steve_model.model_manage.type.unknown");
        };
        this.drawString(font, text, this.x + 155, this.y + (this.height - 8) / 2, 0xFFFFFFFF);
        this.drawString(font, FileSizeUtils.size(this.info.getSize()), this.x + 205, this.y + (this.height - 8) / 2, 0xC0C0C0);
    }

    public void setSelect(boolean select) {
        this.isSelect = select;
    }
}
