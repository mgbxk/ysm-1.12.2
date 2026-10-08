package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.bukkit.message.OpenModelGuiMessage;
import com.elfmcys.yesstevemodel.bukkit.message.SetNpcModelAndTexture;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.client.gui.UiTheme;
import net.minecraft.client.renderer.GlStateManager;
import com.elfmcys.yesstevemodel.network.message.SetModelAndTexture;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import com.elfmcys.yesstevemodel.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.List;

public class TextureButton extends Button {
    private final ResourceLocation modelId;
    private final ResourceLocation textureId;
    private final String name;
    private final EntityPlayer player;

    public TextureButton(int pX, int pY, ResourceLocation modelId, ResourceLocation textureId, EntityPlayer player) {
        super(pX, pY, 54, 102, "", (b) -> {
        });
        this.modelId = modelId;
        this.textureId = textureId;
        this.name = ModelIdUtil.getSubNameFromId(textureId);
        this.player = player;
    }

    @Override
    public void onPress() {
        CapabilityEvent.getModelInfoCap(this.player).ifPresent(cap ->
                cap.setModelAndTexture(this.modelId, this.textureId));
        EntityPlayerSP localPlayer = Minecraft.getMinecraft().player;
        if (this.player.equals(localPlayer)) {
            com.elfmcys.yesstevemodel.client.ClientSession.saveAppearance();
            com.elfmcys.yesstevemodel.client.ClientActions.sendToServer(new SetModelAndTexture(this.modelId, this.textureId));
        } else {
            com.elfmcys.yesstevemodel.client.ClientActions.sendToServer(new SetNpcModelAndTexture(this.modelId, this.textureId, OpenModelGuiMessage.CURRENT_NPC_ID));
        }
    }

    @Override
    protected void renderWidget(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTick) {
        FontRenderer font = mc.fontRenderer;

        boolean selected = CapabilityEvent.getModelInfoCap(player).map(cap -> textureId.equals(cap.getSelectTexture())).orElse(false);
        GlStateManager.disableDepth();
        UiTheme.panel(x, y, width, height, hovered ? 0xFF233548 : UiTheme.CARD, selected ? 0xFF7AE1C4 : hovered ? 0xFF526E85 : UiTheme.BORDER);
        GlStateManager.enableDepth();
        RenderUtil.scissor(this.x, this.y, this.width, this.height - 20);
        RenderUtil.renderTextureButtonEntity(this.x + this.width / 2, this.y + this.height / 2 + 24, 35, mc.player, this.modelId, this.textureId);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        List<String> split = font.listFormattedStringToWidth(this.name, 50);
        if (split.size() > 1) {
            this.drawCenteredString(font, split.get(0), this.x + this.width / 2, this.y + this.height - 19, UiTheme.TEXT);
            this.drawCenteredString(font, split.get(1), this.x + this.width / 2, this.y + this.height - 10, UiTheme.TEXT);
        } else {
            this.drawCenteredString(font, this.name, this.x + this.width / 2, this.y + this.height - 15, UiTheme.TEXT);
        }
        if (selected) { GlStateManager.disableDepth(); UiTheme.pill(font, "已选", x + 4, y + 4, true); GlStateManager.enableDepth(); }
    }
}
