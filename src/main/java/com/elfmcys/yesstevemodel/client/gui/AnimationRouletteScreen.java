package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.client.config.GeneralConfig;
import com.elfmcys.yesstevemodel.client.input.AnimationRouletteKey;
import com.elfmcys.yesstevemodel.client.input.ExtraAnimationKey;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.client.animation.modern.ModernAssets;
import com.elfmcys.yesstevemodel.model.modern.ModernModelOptions;
import net.minecraft.client.gui.GuiButton;
import com.elfmcys.yesstevemodel.client.gui.button.UiButton;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnimationRouletteScreen extends Screen {
    private int x;
    private int y;
    private int radius, panelX, panelY, panelWidth, panelHeight;
    private boolean compactList;
    private static final int INNER_RADIUS = 48;
    private int selectId = -1;
    private @Nullable String[] names;
    private ModernModelOptions options;
    private ResourceLocation model;
    private String group = "";
    private int page;
    private List<ModernModelOptions.Entry> entries;
    private final List<String> labels = new ArrayList<>();

    public AnimationRouletteScreen() {
    }

    @Override
    public void initGui() {
        compactList = width < 360 || height < 340;
        this.x = width / 2;
        this.y = (height + 6) / 2;
        radius = Math.min(112, (height - 140) / 2);
        panelWidth = Math.min(300, width - 44);
        panelHeight = Math.min(250, height - 36);
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;

        if (this.mc != null && this.mc.player != null) {
            CapabilityEvent.getModelInfoCap(this.mc.player).ifPresent(cap -> {
                ResourceLocation modelId = cap.getModelId();
                var info = ClientModelManager.EXTRA_INFO.get(ModelIdUtil.getInfoId(modelId));
                this.names = info == null ? null : info.getExtraAnimationNames();
                model = modelId;
                ModernAssets.Bundle bundle = ModernAssets.MODELS.get(new ResourceLocation(modelId.getNamespace(), modelId.getPath() + "/main"));
                options = bundle == null || bundle.options.group("").isEmpty() ? null : bundle.options;
            });
        }
        buttonList.clear();
        buttonList.add(new UiButton(103, compactList ? panelX + panelWidth - 32 : x + radius - 32, compactList ? panelY + 12 : y - radius - 42, 20, 20, "x"));
        int navigationY = compactList ? panelY + panelHeight - 28 : y + radius + 12;
        if (options != null) {
            List<ModernModelOptions.Entry> all = options.group(group);
            page = Math.max(0, Math.min(page, Math.max(0, (all.size() - 1) / 8)));
            entries = all.subList(page * 8, Math.min(all.size(), page * 8 + 8));
            if (!group.isEmpty()) buttonList.add(new UiButton(100, x - 38, navigationY, 76, 20, "返回分类"));
            if (all.size() > 8) {
                UiButton previous = new UiButton(101, x - 106, navigationY, 54, 20, "<");
                previous.enabled = page > 0;
                buttonList.add(previous);
                UiButton next = new UiButton(102, x + 52, navigationY, 54, 20, ">");
                next.enabled = page < Math.max(0, (all.size() - 1) / 8);
                buttonList.add(next);
            }
        }
        selectId = -1;
        labels.clear();
        int count = options == null ? 8 : entries.size();
        for (int i = 0; i < count; i++) {
            String label = options != null ? entries.get(i).label : names != null && names.length > i && StringUtils.isNotBlank(names[i]) ? names[i] : "动作 " + (i + 1);
            labels.add(UiTheme.modelLabel(label));
        }
        if (compactList) {
            int rowHeight = Math.min(30, (panelHeight - 86) / Math.max(1, (count + 1) / 2) - 6);
            int buttonWidth = (panelWidth - 36) / 2;
            for (int i = 0; i < count; i++) buttonList.add(new UiButton(200 + i, panelX + 14 + (buttonWidth + 8) * (i % 2), panelY + 58 + (rowHeight + 6) * (i / 2), buttonWidth, rowHeight, entryLabel(i)));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        UiTheme.backdrop(width, height); GlStateManager.disableDepth();
        if (compactList) {
            UiTheme.shell(panelX, panelY, panelWidth, panelHeight);
            UiTheme.scaled(fontRenderer, "动作轮盘", panelX + 14, panelY + 14, 1.2f, UiTheme.TEXT);
            boolean groupedPage = !group.isEmpty() && options != null && options.group(group).size() > 8;
            UiTheme.text(fontRenderer, UiTheme.fit(fontRenderer, sectionTitle(), panelWidth - (groupedPage ? 82 : 28)), panelX + 14, panelY + 37, UiTheme.MUTED);
            if (groupedPage) UiTheme.text(fontRenderer, pageLabel(), panelX + panelWidth - 14 - fontRenderer.getStringWidth(pageLabel()), panelY + 37, UiTheme.MUTED);
            if (group.isEmpty() && (options == null || options.group(group).size() <= 8)) UiTheme.centered(fontRenderer, "点击选择 · Z / Esc 关闭", x, panelY + panelHeight - 21, UiTheme.MUTED);
            if (options != null && options.group(group).size() > 8 && group.isEmpty()) UiTheme.centered(fontRenderer, pageLabel(), x, panelY + panelHeight - 21, UiTheme.MUTED);
            super.drawScreen(mouseX, mouseY, partialTicks); GlStateManager.enableDepth(); return;
        }
        drawRoulette(mouseX, mouseY);
        drawRouletteText();
        // Floating title and footer; the ring's centre and all surrounding pixels stay clear.
        UiTheme.shell(x - radius, y - radius - 52, radius * 2, 40);
        UiTheme.text(fontRenderer, UiTheme.fit(fontRenderer, selectId < 0 ? "动作轮盘" : entryLabel(selectId), radius * 2 - 54), x - radius + 12, y - radius - 40, selectId < 0 ? UiTheme.TEXT : UiTheme.ACCENT);
        UiTheme.text(fontRenderer, UiTheme.fit(fontRenderer, sectionTitle(), radius * 2 - 54), x - radius + 12, y - radius - 25, UiTheme.MUTED);
        boolean navigation = !group.isEmpty() || options != null && options.group(group).size() > 8;
        if (navigation) {
            if (options != null && options.group(group).size() > 8 && group.isEmpty()) UiTheme.centered(fontRenderer, pageLabel(), x, y + radius + 18, UiTheme.TEXT);
            UiTheme.panel(x - radius, y + radius + 38, radius * 2, 20, UiTheme.SHELL, UiTheme.BORDER);
            UiTheme.centered(fontRenderer, options != null && options.group(group).size() > 8 && !group.isEmpty() ? pageLabel() + " · Z / Esc 关闭" : "点击确认 · Z / Esc 关闭", x, y + radius + 44, UiTheme.MUTED);
        } else {
            UiTheme.panel(x - radius, y + radius + 12, radius * 2, 22, UiTheme.SHELL, UiTheme.BORDER);
            UiTheme.centered(fontRenderer, selectId < 0 ? "悬停选择 · 点击确认 · Z / Esc 关闭" : (options != null && entries.get(selectId).navigation() ? "点击进入" : "点击播放") + " · Z / Esc 关闭", x, y + radius + 19, UiTheme.MUTED);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        GlStateManager.enableDepth();
    }
    private String sectionTitle() {
        return !group.isEmpty() ? group : model == null ? "快捷动作" : PlayerModelScreen.modelName(model);
    }
    private String pageLabel() {
        return (page + 1) + " / " + ((options.group(group).size() + 7) / 8);
    }
    private String entryLabel(int index) {
        return labels.get(index);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if (button != LEFT_MOUSE_BUTTON) return;
        if (!compactList) {
            updateSelection(mouseX, mouseY);
            if (selectId >= 0) {
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                chooseEntry(selectId); return;
            }
        }
        // Navigation replaces buttonList. Consume the click immediately so vanilla's
        // loop cannot click a newly created action at the same coordinates as well.
        for (GuiButton candidate : buttonList) {
            if (candidate.mousePressed(mc, mouseX, mouseY)) {
                candidate.playPressSound(mc.getSoundHandler());
                actionPerformed(candidate);
                return;
            }
        }
    }
    private void chooseEntry(int index) {
        if (options != null) {
            if (index < 0 || index >= entries.size()) return;
            ModernModelOptions.Entry entry = entries.get(index);
            if (entry.navigation()) {
                String target = entry.target();
                if (target.equals("return")) { group = ""; page = 0; refreshGui(); }
                else if (options.groups.containsKey(target)) { group = target; page = 0; refreshGui(); }
                else if (options.buttons.containsKey(target)) mc.displayGuiScreen(new ModernModelConfigScreen(this, model, options.buttons.get(target)));
            } else if (options.playable.contains(entry.key)) {
                com.elfmcys.yesstevemodel.client.ClientActions.playNamed(model, entry.key); mc.displayGuiScreen(null);
            }
            return;
        }
        if (index < 0 || index >= 8) return;
        com.elfmcys.yesstevemodel.client.ClientActions.playExtra(index);
        if (mc.player != null && GeneralConfig.PRINT_ANIMATION_ROULETTE_MSG) mc.player.sendMessage(new TextComponentTranslation("message.yes_steve_model.model.animation_roulette.play", index));
        mc.displayGuiScreen(null);
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id >= 200 && button.id < 208) { chooseEntry(button.id - 200); return; }
        if (button.id == 103) { mc.displayGuiScreen(null); return; }
        if (button.id == 100) { group = ""; page = 0; }
        else if (button.id == 101) --page;
        else if (button.id == 102) ++page;
        else { super.actionPerformed(button); return; }
        refreshGui();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected boolean canGuiClose(int keyCode) {
        return super.canGuiClose(keyCode) || isKeyActiveIgnoreConflict(AnimationRouletteKey.ANIMATION_ROULETTE_KEY, keyCode);
    }

    private void drawRouletteText() {
        int count = options == null ? 8 : entries.size();
        for (int i = 0; i < count; i++) {
            float angle = (float) ((i + .5) * Math.PI * 2 / count);
            int cx = (int) (x + radius * .74 * MathHelper.cos(angle));
            int cy = (int) (y + radius * .74 * MathHelper.sin(angle));
            int labelWidth = count > 6 ? 56 : 70;
            List<String> lines = fontRenderer.listFormattedStringToWidth(entryLabel(i), labelWidth);
            int lineY = cy - Math.min(2, lines.size()) * 5 - 4;
            for (int j = 0; j < Math.min(2, lines.size()); j++) { UiTheme.centered(fontRenderer, lines.get(j), cx, lineY, i == selectId ? UiTheme.ACCENT : UiTheme.TEXT); lineY += 10; }
            String detail;
            if (options != null) detail = entries.get(i).navigation() ? "进入" : "播放";
            else {
                KeyBinding key = ExtraAnimationKey.EXTRA_ANIMATION_KEYS.get(i);
                detail = key.getKeyCode() == Keyboard.KEY_NONE ? "点击播放" : key.getDisplayName();
            }
            UiTheme.scaled(fontRenderer, detail, cx - fontRenderer.getStringWidth(detail) / 2, cy + 9, 1f, UiTheme.MUTED);
        }
    }
    private void updateSelection(int mouseX, int mouseY) {
        int count = options == null ? 8 : entries.size();
        double theta = Math.atan2(mouseY - y, mouseX - x);
        if (theta < 0) theta += Math.PI * 2;
        double distance = Math.hypot(mouseX - x, mouseY - y);
        int index = count == 0 ? -1 : (int) (theta * count / (Math.PI * 2));
        double local = count == 0 ? 0 : theta - index * Math.PI * 2 / count;
        selectId = index >= 0 && index < count && distance > INNER_RADIUS && distance < radius && local > .03 && local < Math.PI * 2 / count - .03 ? index : -1;
    }
    private void drawRoulette(int mouseX, int mouseY) {
        updateSelection(mouseX, mouseY);
        GlStateManager.disableDepth(); GlStateManager.disableTexture2D(); GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        Tessellator tessellator = Tessellator.getInstance(); BufferBuilder b = tessellator.getBuffer();
        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        int count = options == null ? 8 : entries.size();
        for (int i = 0; i < count; i++) {
            float start = (float) (Math.PI * 2 * i / count + .03);
            float end = (float) (Math.PI * 2 * (i + 1) / count - .03);
            drawFan(b, INNER_RADIUS, radius, start, end, i == selectId ? 0xEE265147 : 0xD6172535);
            drawFan(b, INNER_RADIUS, INNER_RADIUS + 1, start, end, i == selectId ? 0xFF7AE1C4 : 0xAD50657A);
            drawFan(b, radius - (i == selectId ? 2 : 1), radius, start, end, i == selectId ? 0xFF7AE1C4 : 0xAD50657A);
        }
        tessellator.draw(); GlStateManager.disableBlend(); GlStateManager.enableTexture2D(); GlStateManager.color(1, 1, 1, 1);
    }

    private void drawFan(BufferBuilder builder, float rIn, float rOut, float startDeg, float endDeg, int color) {
        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        int slices = Math.max(1, (int) Math.ceil((endDeg - startDeg) * 40));
        for (int i = 0; i < slices; i++) {
            float start = startDeg + (endDeg - startDeg) * i / slices;
            float end = startDeg + (endDeg - startDeg) * (i + 1) / slices;
            builder.pos(this.x + rOut * MathHelper.cos(start), this.y + rOut * MathHelper.sin(start), this.zLevel).color(red, green, blue, alpha).endVertex();
            builder.pos(this.x + rIn * MathHelper.cos(start), this.y + rIn * MathHelper.sin(start), this.zLevel).color(red, green, blue, alpha).endVertex();
            builder.pos(this.x + rIn * MathHelper.cos(end), this.y + rIn * MathHelper.sin(end), this.zLevel).color(red, green, blue, alpha).endVertex();
            builder.pos(this.x + rOut * MathHelper.cos(end), this.y + rOut * MathHelper.sin(end), this.zLevel).color(red, green, blue, alpha).endVertex();
        }
    }
}
