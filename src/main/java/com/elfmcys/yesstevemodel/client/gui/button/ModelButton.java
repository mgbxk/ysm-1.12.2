package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.bukkit.message.OpenModelGuiMessage;
import com.elfmcys.yesstevemodel.bukkit.message.SetNpcModelAndTexture;
import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.client.config.GeneralConfig;
import com.elfmcys.yesstevemodel.client.gui.UiTheme;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.ExtraInfo;
import com.elfmcys.yesstevemodel.network.message.SetModelAndTexture;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import com.elfmcys.yesstevemodel.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ModelButton extends Button {
    private final static ResourceLocation ICON = new ResourceLocation(YesSteveModel.MOD_ID, "texture/icon.png");
    private final Pair<ResourceLocation, List<ResourceLocation>> modelInfo;
    private final boolean needAuth;
    private final @Nullable List<String> tooltips;
    private final EntityPlayer player;
    private final String modelName;
    private final String previewAnimation;
    private final boolean disablePreviewRotation;
    private final @Nullable ResourceLocation backgroundTexture;
    private final @Nullable ResourceLocation foregroundTexture;

    /**
     * @param modelInfo Model Id, Textures
     */
    public ModelButton(int pX, int pY, boolean needAuth, Pair<ResourceLocation, List<ResourceLocation>> modelInfo, @Nullable List<String> tooltips, EntityPlayer player) {
        this(pX, pY, 52, 90, needAuth, modelInfo, tooltips, player);
    }

    public ModelButton(int pX, int pY, int width, int height, boolean needAuth, Pair<ResourceLocation, List<ResourceLocation>> modelInfo, @Nullable List<String> tooltips, EntityPlayer player) {
        super(pX, pY, width, height, modelInfo.getLeft().getPath(), (b) -> {
        });
        this.modelInfo = modelInfo;
        this.needAuth = needAuth;
        this.tooltips = tooltips;
        this.player = player;
        final ResourceLocation modelId = this.modelInfo.getLeft();
        final ExtraInfo extraInfo = ClientModelManager.EXTRA_INFO.get(ModelIdUtil.getInfoId(modelId));
        this.previewAnimation = extraInfo.getPreviewAnimation() != null ? extraInfo.getPreviewAnimation() : "idle";
        this.disablePreviewRotation = extraInfo.getDisablePreviewRotation();
        this.modelName = extraInfo.getName() != null ? extraInfo.getName() : "";
        final String guiBackground = extraInfo.getGuiBackground();
        this.backgroundTexture = guiBackground != null && !guiBackground.isEmpty() ?
                ModelIdUtil.getSubModelId(modelId, guiBackground) : null;
        final String guiForeground = extraInfo.getGuiForeground();
        this.foregroundTexture = guiForeground != null && !guiForeground.isEmpty() ?
                ModelIdUtil.getSubModelId(modelId, guiForeground) : null;
    }

    @Override
    public void onPress() {
        if (this.needAuth) {
            return;
        }
        CapabilityEvent.getModelInfoCap(this.player).ifPresent(cap ->
                cap.setModelAndTexture(this.modelInfo.getLeft(), this.modelInfo.getRight().get(0)));
        EntityPlayerSP localPlayer = Minecraft.getMinecraft().player;
        if (this.player.equals(localPlayer)) {
            com.elfmcys.yesstevemodel.client.ClientSession.saveAppearance();
            com.elfmcys.yesstevemodel.client.ClientActions.sendToServer(new SetModelAndTexture(this.modelInfo.getLeft(), this.modelInfo.getRight().get(0)));
        } else {
            com.elfmcys.yesstevemodel.client.ClientActions.sendToServer(new SetNpcModelAndTexture(this.modelInfo.getLeft(), this.modelInfo.getRight().get(0), OpenModelGuiMessage.CURRENT_NPC_ID));
        }
    }

    @Override
    protected void renderWidget(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTick) {
        final FontRenderer font = mc.fontRenderer;
        GlStateManager.disableDepth();
        boolean selected = CapabilityEvent.getModelInfoCap(player).map(cap -> cap.getModelId().equals(modelInfo.getLeft())).orElse(false);
        UiTheme.panel(x, y, width, height, hovered ? 0xFF233548 : UiTheme.CARD, selected ? 0xFF7AE1C4 : hovered ? 0xFF526E85 : UiTheme.BORDER);
        // 背景图
        if (this.backgroundTexture != null) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            mc.getTextureManager().bindTexture(this.backgroundTexture);
            drawScaledCustomSizeModalRect(x + 2, y + 2, 0, 0, 52, 90, width - 4, height - 4, 52, 90);
            GlStateManager.disableBlend();
        }
        // 玩家模型
        GlStateManager.enableDepth();
        RenderUtil.scissor(this.x + 2, this.y + 2, this.width - 4, this.height - 25);
        RenderUtil.renderEntityInInventory(this.x + this.width / 2, this.y + this.height - 27, Math.min(37, (height - 28) / 2), mc.player, this.modelInfo.getLeft(), this.modelInfo.getRight().get(0), custom -> {
            if (!this.previewAnimation.isEmpty() && !custom.hasPreviewAnimation(this.previewAnimation)) {
                custom.setPreviewAnimation(this.previewAnimation);
            }
        }, this.disablePreviewRotation);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.disableDepth();
        // 前景图
        if (this.foregroundTexture != null) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            mc.getTextureManager().bindTexture(this.foregroundTexture);
            drawScaledCustomSizeModalRect(x + 2, y + 2, 0, 0, 52, 90, width - 4, height - 4, 52, 90);
            GlStateManager.disableBlend();
        }
        // 文字
        final String modelName = GeneralConfig.SHOW_MODEL_ID_FIRST || this.modelName.isEmpty() ? this.displayString : this.modelName;
        drawRect(x + 2, y + height - 24, x + width - 2, y + height - 2, 0xE9101823);
        List<String> split = font.listFormattedStringToWidth(modelName.replaceAll("([\\p{IsHan}])([A-Za-z])", "$1 $2"), width - 10);
        if (split.size() > 1) {
            UiTheme.centered(font, split.get(0), x + width / 2, y + height - 21, UiTheme.TEXT);
            UiTheme.centered(font, UiTheme.fit(font, split.get(1), width - 10), x + width / 2, y + height - 11, UiTheme.TEXT);
        } else {
            UiTheme.centered(font, modelName, x + width / 2, y + height - 16, UiTheme.TEXT);
        }
        // 悬停边框
        if (selected) UiTheme.pill(font, "已选", x + 5, y + 5, true);
        // 锁定遮罩
        if (this.needAuth) {
            this.drawGradientRect(this.x, this.y, this.x + this.width, this.y + this.height, 0x9f_222222, 0x9f_222222);
        }
        // 收藏图标
        CapabilityEvent.getStarModelsCap(mc.player).ifPresent(cap -> {
            if (cap.containModel(this.modelInfo.getLeft())) {
                mc.getTextureManager().bindTexture(ICON);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                GlStateManager.enableDepth();
                this.drawTexturedModalRect(this.x + this.width - 14, this.y, 16, 0, 16, 16);
            }
        });
        GlStateManager.enableDepth();
    }

    public void renderComponentTooltip(GuiScreen screen, int pMouseX, int pMouseY) {
        if (this.isMouseOver() && this.tooltips != null) {
            screen.drawHoveringText(this.tooltips, pMouseX, pMouseY);
        }
    }

    @Override
    public boolean mousePressed(@Nonnull Minecraft mc, int mouseX, int mouseY) {
        return !this.needAuth && super.mousePressed(mc, mouseX, mouseY);
    }
}
