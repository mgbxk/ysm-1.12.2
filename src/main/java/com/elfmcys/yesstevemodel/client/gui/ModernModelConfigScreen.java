package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.gui.button.UiButton;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.model.modern.ModernModelOptions;
import com.elfmcys.yesstevemodel.network.message.SetModelSetting;
import com.elfmcys.yesstevemodel.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/** Model-defined controls, with live preview and server-backed persistence. */
public final class ModernModelConfigScreen extends Screen {
    private final Screen parent;
    private final ResourceLocation model;
    private final ModernModelOptions.ConfigButton definition;
    private final String title;
    private int left, top, panelWidth, panelHeight, controlsX, controlsWidth, viewportTop, viewportHeight, scroll, contentHeight;
    private RangeButton dragging;
    public ModernModelConfigScreen(Screen parent, ResourceLocation model, ModernModelOptions.ConfigButton definition) {
        this.parent = parent; this.model = model; this.definition = definition; this.title = UiTheme.modelLabel(definition.name);
    }
    @Override public void initGui() {
        panelWidth = Math.min(470, width - 44); panelHeight = Math.min(300, height - 40);
        left = (width - panelWidth) / 2; top = (height - panelHeight) / 2;
        int sideWidth = Math.min(126, panelWidth / 3);
        controlsX = left + sideWidth + 28; controlsWidth = panelWidth - sideWidth - 48;
        viewportTop = top + 60; viewportHeight = panelHeight - 98;
        buttonList.clear();
        int offset = 0;
        for (int i = 0; i < definition.forms.size(); i++) {
            ModernModelOptions.Form form = definition.forms.get(i);
            boolean pair = form.type.equals("checkbox") && i + 1 < definition.forms.size() && definition.forms.get(i + 1).type.equals("checkbox") && controlsWidth >= 220;
            int rowHeight = form.type.equals("checkbox") ? 32 : 42;
            int controlWidth = pair ? (controlsWidth - 8) / 2 : controlsWidth;
            buttonList.add(form.type.equals("range") ? new RangeButton(i, controlsX, viewportTop + offset - scroll, controlWidth, form) : new FormButton(i, controlsX, viewportTop + offset - scroll, controlWidth, rowHeight, form));
            if (pair) { i++; buttonList.add(new FormButton(i, controlsX + controlWidth + 8, viewportTop + offset - scroll, controlWidth, rowHeight, definition.forms.get(i))); }
            offset += rowHeight + 6;
        }
        contentHeight = Math.max(0, offset - 6);
        int clamped = Math.max(0, Math.min(scroll, Math.max(0, contentHeight - viewportHeight)));
        if (clamped != scroll) { int delta = scroll - clamped; for (GuiButton button : buttonList) button.y += delta; scroll = clamped; }
        buttonList.add(new UiButton(1000, left + panelWidth - 81, top + panelHeight - 28, 65, 18, "完成"));
    }
    private double value(ModernModelOptions.Form form) {
        return mc.player == null ? 0 : CapabilityEvent.getModelInfoCap(mc.player).map(cap -> cap.getModelSettings().getOrDefault(form.variable, 0d)).orElse(0d);
    }
    private String formatted(ModernModelOptions.Form form) {
        if (form.type.equals("radio")) {
            List<String> labels = new ArrayList<>(form.labels.keySet());
            return labels.isEmpty() ? "" : labels.get((int) form.normalize(value(form)));
        }
        return String.format(Locale.ROOT, form.step >= 1 ? "%.0f" : "%.2f", value(form));
    }
    private void change(ModernModelOptions.Form form, double value, boolean send) {
        value = form.normalize(value); final double normalized = value;
        CapabilityEvent.getModelInfoCap(mc.player).ifPresent(cap -> form.apply(normalized, cap.getModelSettings()).forEach(cap::setModelSetting));
        if (send) {
            com.elfmcys.yesstevemodel.client.ClientActions.sendToServer(new SetModelSetting(model.toString(), form.variable, value));
            com.elfmcys.yesstevemodel.client.ClientSession.saveAppearance();
        }
    }
    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1000) { mc.displayGuiScreen(parent); return; }
        if (!(button instanceof FormButton)) return;
        ModernModelOptions.Form form = definition.forms.get(button.id);
        if (button instanceof RangeButton) { dragging = (RangeButton) button; return; }
        change(form, form.type.equals("checkbox") ? (value(form) == 0 ? 1 : 0) : (value(form) + 1) % Math.max(1, form.labels.size()), true);
    }
    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        UiTheme.backdrop(width, height); GlStateManager.disableDepth(); UiTheme.shell(left, top, panelWidth, panelHeight);
        UiTheme.scaled(fontRenderer, UiTheme.fit(fontRenderer, title, panelWidth / 2), left + 18, top + 16, 1.35f, UiTheme.TEXT);
        UiTheme.text(fontRenderer, "调整外观，实时预览", left + 18, top + 39, UiTheme.MUTED);
        int sideWidth = controlsX - left - 28;
        UiTheme.panel(left + 14, viewportTop, sideWidth, viewportHeight, UiTheme.PANEL, UiTheme.BORDER);
        UiTheme.text(fontRenderer, "角色预览", left + 26, viewportTop + 13, UiTheme.MUTED);
        if (mc.player != null) {
            float scale = Math.min(95, Math.max(16, (viewportHeight - 68) / 1.9f));
            RenderUtil.scissor(left + 16, viewportTop + 30, sideWidth - 4, viewportHeight - 54);
            RenderUtil.renderPlayerEntity(mc.player, left + 14 + sideWidth / 2d - scale * .5, viewportTop + viewportHeight - 32 - scale * 2, scale, 0, 100);
            GL11.glDisable(GL11.GL_SCISSOR_TEST); GlStateManager.disableDepth();
        }
        UiTheme.centered(fontRenderer, UiTheme.fit(fontRenderer, PlayerModelScreen.modelName(model), sideWidth - 16), left + 14 + sideWidth / 2, viewportTop + viewportHeight - 20, UiTheme.TEXT);
        RenderUtil.scissor(controlsX, viewportTop, controlsWidth, viewportHeight);
        for (GuiButton button : buttonList) if (button instanceof FormButton && button.y + button.height > viewportTop && button.y < viewportTop + viewportHeight) button.drawButton(mc, mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        for (GuiButton button : buttonList) if (!(button instanceof FormButton)) button.drawButton(mc, mouseX, mouseY, partialTicks);
        if (contentHeight > viewportHeight) {
            int thumb = Math.max(18, viewportHeight * viewportHeight / contentHeight);
            int thumbY = viewportTop + (viewportHeight - thumb) * scroll / Math.max(1, contentHeight - viewportHeight);
            UiTheme.rounded(controlsX + controlsWidth + 5, viewportTop, 3, viewportHeight, 1, UiTheme.BORDER);
            UiTheme.rounded(controlsX + controlsWidth + 5, thumbY, 3, thumb, 1, 0xFF559985);
        }
        UiTheme.text(fontRenderer, contentHeight > viewportHeight ? (controlsWidth < 300 ? "滚轮查看更多" : "滚轮查看更多 · 修改自动保存") : "修改自动保存", controlsX, top + panelHeight - 23, UiTheme.MUTED);
        if (insideViewport(mouseX, mouseY)) for (GuiButton button : buttonList) if (button instanceof FormButton && button.isMouseOver()) {
            ModernModelOptions.Form form = ((FormButton) button).form;
            if (!form.description.isEmpty()) drawHoveringText(listLineBreakStringToWidth(form.description, 210), mouseX, mouseY);
        }
        GlStateManager.enableDepth();
    }
    private boolean insideViewport(int mouseX, int mouseY) { return mouseX >= controlsX && mouseX < controlsX + controlsWidth && mouseY >= viewportTop && mouseY < viewportTop + viewportHeight; }
    @Override protected void mouseDragged(int mouseX, int mouseY, int button, int dx, int dy) { if (dragging != null) dragging.move(mouseX); }
    @Override protected void mouseReleased(int mouseX, int mouseY, int button) {
        if (dragging != null) { change(dragging.form, value(dragging.form), true); dragging = null; }
        super.mouseReleased(mouseX, mouseY, button);
    }
    @Override protected void mouseScrolled(int mouseX, int mouseY, int delta) { if (dragging == null && insideViewport(mouseX, mouseY)) { scroll -= delta * 38; refreshGui(); } }
    @Override protected void keyTyped(char character, int key) throws IOException { if (key == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent); else super.keyTyped(character, key); }
    @Override public boolean doesGuiPauseGame() { return false; }
    @Override public void onGuiClosed() { if (dragging != null && mc.player != null) change(dragging.form, value(dragging.form), true); }
    private class FormButton extends GuiButton {
        final ModernModelOptions.Form form;
        FormButton(int id, int x, int y, int width, int height, ModernModelOptions.Form form) { super(id, x, y, width, height, ""); this.form = form; }
        @Override public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) { return insideViewport(mouseX, mouseY) && super.mousePressed(minecraft, mouseX, mouseY); }
        @Override public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            hovered = insideViewport(mouseX, mouseY) && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
            UiTheme.panel(x, y, width, height, hovered ? 0xFF233548 : UiTheme.CARD, hovered ? 0xFF526E85 : UiTheme.BORDER);
            if (form.type.equals("checkbox")) {
                UiTheme.text(fontRenderer, UiTheme.fit(fontRenderer, form.title, width - 50), x + 10, y + 12, UiTheme.TEXT);
                boolean on = value(form) != 0;
                UiTheme.rounded(x + width - 37, y + 9, 27, 14, 7, on ? 0xFF427F6A : UiTheme.BORDER);
                UiTheme.rounded(x + width - (on ? 22 : 35), y + 11, 10, 10, 5, on ? 0xFFBDF9DD : 0xFF91A3BA);
            } else {
                UiTheme.text(fontRenderer, UiTheme.fit(fontRenderer, form.title, width - 75), x + 11, y + 10, UiTheme.TEXT);
                if (form.type.equals("radio")) UiTheme.text(fontRenderer, UiTheme.fit(fontRenderer, formatted(form) + "  >", width - 22), x + 11, y + 26, UiTheme.ACCENT);
                else UiTheme.text(fontRenderer, formatted(form), x + width - fontRenderer.getStringWidth(formatted(form)) - 11, y + 10, UiTheme.ACCENT);
            }
        }
    }
    private final class RangeButton extends FormButton {
        RangeButton(int id, int x, int y, int width, ModernModelOptions.Form form) { super(id, x, y, width, 42, form); }
        void move(int mouseX) { double fraction = Math.max(0, Math.min(1, (mouseX - x - 12d) / Math.max(1, width - 24))); change(form, form.min + fraction * (form.max - form.min), false); }
        @Override public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) { if (!super.mousePressed(minecraft, mouseX, mouseY)) return false; move(mouseX); return true; }
        @Override public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            super.drawButton(minecraft, mouseX, mouseY, partialTicks);
            int offset = (int) ((width - 24) * (form.max == form.min ? 0 : (form.normalize(value(form)) - form.min) / (form.max - form.min)));
            UiTheme.rounded(x + 12, y + 30, width - 24, 3, 1, UiTheme.BORDER);
            UiTheme.rounded(x + 12, y + 30, offset, 3, 1, 0xFF7AE1C4);
            UiTheme.rounded(x + 8 + offset, y + 27, 8, 8, 4, 0xFFBDF9DD);
        }
    }
}
