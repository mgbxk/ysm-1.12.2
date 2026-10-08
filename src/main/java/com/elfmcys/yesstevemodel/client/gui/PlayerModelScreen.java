package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.client.config.GeneralConfig;
import com.elfmcys.yesstevemodel.client.gui.button.FlatColorButton;
import com.elfmcys.yesstevemodel.client.gui.button.FlatIconButton;
import com.elfmcys.yesstevemodel.client.gui.button.ModelButton;
import com.elfmcys.yesstevemodel.client.gui.button.StarButton;
import com.elfmcys.yesstevemodel.client.input.PlayerModelScreenKey;
import com.elfmcys.yesstevemodel.config.Config;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.ExtraInfo;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import com.elfmcys.yesstevemodel.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PlayerModelScreen extends Screen {
    protected final EntityPlayer player;
    private final List<ResourceLocation> modelOrderList = new ArrayList<>();
    private GuiTextField textField;
    private Category category = Category.ALL;
    private int page, maxPage, pageSize, x, y, panelWidth, panelHeight;
    private int sideWidth, gridX, gridWidth, columns, rows, cardWidth, cardHeight;
    private boolean compact;

    public PlayerModelScreen() { this(Minecraft.getMinecraft().player); }
    public PlayerModelScreen(EntityPlayer player) { this.player = player; }
    static String modelName(ResourceLocation model) {
        ExtraInfo info = ClientModelManager.EXTRA_INFO.get(ModelIdUtil.getInfoId(model));
        return info != null && info.getName() != null && !info.getName().isEmpty() ? info.getName() : model.getPath();
    }
    private void calculateModelList() {
        modelOrderList.clear();
        String search = textField == null ? "" : textField.getText().toLowerCase(Locale.ROOT);
        for (ResourceLocation id : ClientModelManager.MODELS.keySet()) {
            if (category == Category.STAR && !CapabilityEvent.getStarModelsCap(player).map(cap -> cap.containModel(id)).orElse(false)) continue;
            if (category == Category.AUTH && ClientModelManager.AUTH_MODELS.contains(id.getPath()) && !CapabilityEvent.getAuthModelsCap(player).map(cap -> cap.containModel(id)).orElse(false)) continue;
            if (id.toString().toLowerCase(Locale.ROOT).contains(search) || modelName(id).toLowerCase(Locale.ROOT).contains(search)) modelOrderList.add(id);
        }
        modelOrderList.sort(ResourceLocation::compareTo);
        maxPage = Math.max(0, (modelOrderList.size() - 1) / pageSize);
        page = Math.max(0, Math.min(page, maxPage));
    }
    @Override public void initGui() {
        panelWidth = Math.min(488, width - 44); panelHeight = Math.min(300, height - 40);
        x = (width - panelWidth) / 2; y = (height - panelHeight) / 2;
        compact = panelHeight < 260;
        sideWidth = Math.max(92, Math.min(126, panelWidth / 3));
        gridX = x + sideWidth + 24; gridWidth = panelWidth - sideWidth - 36;
        columns = Math.max(1, Math.min(5, (gridWidth + 8) / 68)); rows = panelHeight >= 300 ? 2 : 1;
        pageSize = columns * rows; cardWidth = (gridWidth - 8 * (columns - 1)) / columns;
        cardHeight = Math.min(106, (panelHeight - 132 - 8 * (rows - 1)) / rows);
        calculateModelList();
        String search = textField == null ? "" : textField.getText();
        boolean focus = textField != null && textField.isFocused();
        textField = new GuiTextField(0, fontRenderer, gridX + 10, y + 21, gridWidth - 48, 12);
        textField.setEnableBackgroundDrawing(false); textField.setMaxStringLength(128);
        textField.setTextColor(UiTheme.TEXT); textField.setText(search); textField.setFocused(focus); textField.setCursorPositionEnd();
        buttonList.clear();
        addButton(new FlatColorButton(x + panelWidth - 30, y + 16, 18, 20, "x", b -> mc.displayGuiScreen(null)));
        String[] labels = {"全部模型", "可使用", "收藏"};
        int tabWidth = Math.min(59, Math.max(30, (gridWidth - 12) / 3));
        for (int i = 0; i < Category.values().length; i++) {
            Category tab = Category.values()[i];
            FlatColorButton categoryButton = new FlatColorButton(gridX + i * (tabWidth + 5), y + 58, tabWidth, 20, labels[i], b -> { category = tab; page = 0; refreshGui(); });
            categoryButton.setSelect(category == tab); addButton(categoryButton);
        }
        addButton(new FlatColorButton(x + 22, y + panelHeight - (compact ? 54 : 84), sideWidth - 20, 22, "材质与动作预览", b -> CapabilityEvent.getModelInfoCap(player).ifPresent(cap -> {
            List<ResourceLocation> textures = ClientModelManager.MODELS.get(cap.getModelId());
            if (textures != null) mc.displayGuiScreen(new PlayerTextureScreen(this, cap.getModelId(), textures));
        })));
        if (!compact) {
            FlatColorButton animation = new FlatColorButton(x + 22, y + panelHeight - 56, sideWidth - 46, 20, "打开动作轮盘", b -> mc.displayGuiScreen(new AnimationRouletteScreen()));
            animation.enabled = player.equals(mc.player); addButton(animation);
            addButton(new StarButton(x + sideWidth - 14, y + panelHeight - 56));
        }
        FlatColorButton ids = new FlatColorButton(x + 12, y + panelHeight - 29, 24, 18, "ID", b -> { GeneralConfig.SHOW_MODEL_ID_FIRST = !GeneralConfig.SHOW_MODEL_ID_FIRST; Config.save(); refreshGui(); });
        ids.setSelect(GeneralConfig.SHOW_MODEL_ID_FIRST); addButton(ids);
        addButton(new FlatIconButton(x + 41, y + panelHeight - 29, 18, 18, 80, 0, b -> mc.displayGuiScreen(new OpenModelFolderScreen(this))).setTooltips("gui.yes_steve_model.open_model_folder.open"));
        addButton(new FlatIconButton(x + 64, y + panelHeight - 29, 18, 18, 0, 16, b -> mc.displayGuiScreen(new DownloadScreen(this))).setTooltips("gui.yes_steve_model.download"));
        addButton(new FlatIconButton(x + 87, y + panelHeight - 29, 18, 18, 16, 16, b -> mc.displayGuiScreen(new ConfigScreen(this))).setTooltips("gui.yes_steve_model.config"));
        FlatColorButton previous = new FlatColorButton(gridX + gridWidth - 99, y + panelHeight - 29, 24, 19, "<", b -> { page--; refreshGui(); }); previous.enabled = page > 0; addButton(previous);
        FlatColorButton next = new FlatColorButton(gridX + gridWidth - 24, y + panelHeight - 29, 24, 19, ">", b -> { page++; refreshGui(); }); next.enabled = page < maxPage; addButton(next);
        for (int i = 0; i < pageSize && i + page * pageSize < modelOrderList.size(); i++) {
            ResourceLocation id = modelOrderList.get(i + page * pageSize);
            boolean locked = ClientModelManager.AUTH_MODELS.contains(id.getPath()) && !CapabilityEvent.getAuthModelsCap(player).map(cap -> cap.containModel(id)).orElse(false);
            addButton(new ModelButton(gridX + (cardWidth + 8) * (i % columns), y + 86 + (cardHeight + 8) * (i / columns), cardWidth, cardHeight, locked, Pair.of(id, ClientModelManager.MODELS.get(id)), ClientModelManager.METADATA.get(ModelIdUtil.getInfoId(id)), player));
        }
    }
    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        UiTheme.backdrop(width, height); GlStateManager.disableDepth();
        UiTheme.shell(x, y, panelWidth, panelHeight);
        UiTheme.rounded(x + 16, y + 14, 24, 24, 7, UiTheme.SELECTED);
        UiTheme.scaled(fontRenderer, "Y", x + 23, y + 20, 1.25f, UiTheme.ACCENT);
        UiTheme.scaled(fontRenderer, "模型库", x + 48, y + 18, 1.35f, UiTheme.TEXT);
        UiTheme.text(fontRenderer, com.elfmcys.yesstevemodel.client.ClientSession.isLocal() ? "本地模式 · 仅自己可见" : "服务器同步", x + 16, y + 42, UiTheme.MUTED);
        UiTheme.panel(gridX, y + 16, gridWidth - 36, 22, UiTheme.PANEL, textField.isFocused() ? 0xFF559985 : UiTheme.BORDER);
        textField.drawTextBox();
        if (textField.getText().isEmpty() && !textField.isFocused()) UiTheme.text(fontRenderer, UiTheme.fit(fontRenderer, "搜索名称或模型 ID...", textField.width), gridX + 10, y + 23, UiTheme.MUTED);
        UiTheme.panel(x + 12, y + 55, sideWidth, panelHeight - 88, UiTheme.PANEL, UiTheme.BORDER);
        UiTheme.text(fontRenderer, "当前角色", x + 23, y + 66, UiTheme.MUTED);
        int previewHeight = Math.max(36, panelHeight - (compact ? 170 : 208));
        UiTheme.rounded(x + 23, y + 85, sideWidth - 22, previewHeight, 6, 0xFF111D2B);
        GlStateManager.enableDepth();
        RenderUtil.scissor(x + 23, y + 85, sideWidth - 22, previewHeight);
        float previewScale = Math.min(62, (previewHeight - 10) / 2.1f);
        RenderUtil.renderPlayerEntity(player, x + 12 + sideWidth / 2 - previewScale * .5f,
                y + 85 + previewHeight - 5 - previewScale * 2, previewScale, -12, 50);
        GL11.glDisable(GL11.GL_SCISSOR_TEST); GlStateManager.disableDepth();
        CapabilityEvent.getModelInfoCap(player).ifPresent(cap -> {
            UiTheme.centered(fontRenderer, UiTheme.fit(fontRenderer, modelName(cap.getModelId()), sideWidth - 16), x + 12 + sideWidth / 2, y + panelHeight - (compact ? 79 : 119), UiTheme.TEXT);
            if (!compact) {
                List<ResourceLocation> textures = ClientModelManager.MODELS.get(cap.getModelId());
                UiTheme.centered(fontRenderer, (textures == null ? 0 : textures.size()) + " 套材质 · 已装备", x + 12 + sideWidth / 2, y + panelHeight - 102, UiTheme.ACCENT);
            }
        });
        if (gridWidth > 240) UiTheme.text(fontRenderer, modelOrderList.size() + " 个模型", gridX + gridWidth - 63, y + 64, UiTheme.MUTED);
        if (modelOrderList.isEmpty()) {
            UiTheme.centered(fontRenderer, category == Category.STAR ? "还没有收藏的模型" : "没有找到匹配的模型", gridX + gridWidth / 2, y + panelHeight / 2, UiTheme.TEXT);
            UiTheme.centered(fontRenderer, "试试其他名称或分类", gridX + gridWidth / 2, y + panelHeight / 2 + 18, UiTheme.MUTED);
        }
        if (gridWidth > 220) UiTheme.text(fontRenderer, com.elfmcys.yesstevemodel.client.ClientSession.isLocal() ? "本地外观已自动保存" : "点击模型立即使用", gridX, y + panelHeight - 23, UiTheme.MUTED);
        UiTheme.centered(fontRenderer, (page + 1) + " / " + (maxPage + 1), gridX + gridWidth - 50, y + panelHeight - 23, UiTheme.TEXT);
        super.drawScreen(mouseX, mouseY, partialTicks);
        buttonList.stream().filter(b -> b instanceof FlatIconButton).forEach(b -> ((FlatIconButton) b).renderToolTip(this, mouseX, mouseY));
        buttonList.stream().filter(b -> b instanceof ModelButton).forEach(b -> ((ModelButton) b).renderComponentTooltip(this, mouseX, mouseY));
        GlStateManager.enableDepth();
    }
    @Override public void updateScreen() { if (textField != null) textField.updateCursorCounter(); }
    @Override public void mouseClicked(int mouseX, int mouseY, int button) throws IOException { textField.mouseClicked(mouseX, mouseY, button); super.mouseClicked(mouseX, mouseY, button); }
    @Override public void keyTyped(char character, int key) throws IOException {
        String previous = textField.getText();
        if (textfieldKey(character, key)) { if (!Objects.equals(previous, textField.getText())) { page = 0; refreshGui(); } return; }
        super.keyTyped(character, key);
    }
    private boolean textfieldKey(char character, int key) { return textField != null && textField.textboxKeyTyped(character, key); }
    @Override public void mouseScrolled(int mouseX, int mouseY, int delta) {
        if (mouseX < gridX || mouseX >= gridX + gridWidth || mouseY < y + 55 || mouseY >= y + panelHeight) return;
        int next = Math.max(0, Math.min(maxPage, page - delta));
        if (next != page) { page = next; refreshGui(); }
    }
    @Override public boolean doesGuiPauseGame() { return false; }
    @Override protected boolean canGuiClose(int keyCode) { return super.canGuiClose(keyCode) || isKeyActiveIgnoreConflict(PlayerModelScreenKey.PLAYER_MODEL_KEY, keyCode); }
    private enum Category { ALL, AUTH, STAR }
}
