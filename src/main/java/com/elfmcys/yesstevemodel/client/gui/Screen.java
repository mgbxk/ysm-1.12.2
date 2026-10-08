package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.gui.button.Button;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Screen extends GuiScreen {
    @Override
    protected void actionPerformed(@Nonnull GuiButton guiButton) throws IOException {
        if (guiButton instanceof Button button) {
            button.onPress();
            return;
        }
        super.actionPerformed(guiButton);
    }

    protected static final int LEFT_MOUSE_BUTTON = 0;
    protected static final int RIGHT_MOUSE_BUTTON = 1;
    private int lastMouseX;
    private int laseMouseY;

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if (button == LEFT_MOUSE_BUTTON || button == RIGHT_MOUSE_BUTTON) {
            this.lastMouseX = mouseX;
            this.laseMouseY = mouseY;
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceLastClick) {
        this.mouseDragged(mouseX, mouseY, button, mouseX - this.lastMouseX, mouseY - this.laseMouseY);
        this.lastMouseX = mouseX;
        this.laseMouseY = mouseY;
    }

    protected void mouseDragged(int mouseX, int mouseY, int button, int deltaX, int deltaY) {
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0) {
            this.mouseScrolled(
                    Mouse.getEventX() * this.width / this.mc.displayWidth,
                    this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1,
                    dWheel > 0 ? 1 : -1
            );
        }
    }

    protected void mouseScrolled(int mouseX, int mouseY, int delta) {
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.canGuiClose(keyCode)) super.keyTyped(typedChar, Keyboard.KEY_ESCAPE);
        else if (keyCode != Keyboard.KEY_ESCAPE) super.keyTyped(typedChar, keyCode); // 保留兼容
    }

    protected boolean canGuiClose(int keyCode) {
        return keyCode == Keyboard.KEY_ESCAPE;
    }

    /**
     * 清除 {@link #buttonList} 并发起 Forge 事件，供 Gui 自己调用。其实就是把 {@link #initGui()} 包装了一下。
     */
    protected void refreshGui() {
        if (!MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.InitGuiEvent.Pre(this, this.buttonList))) {
            this.buttonList.clear();
            this.initGui();
        }
        MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.InitGuiEvent.Post(this, this.buttonList));
    }

    /**
     * List string to width and draw text which contains "\n" or "\\n".
     *
     * @return The relative y pos of the last line.
     */
    @SuppressWarnings("UnusedReturnValue")
    public int drawWordWrap(@Nullable String text, int x, int y, int wrapWidth, int color) {
        int currentY = 0;
        if (text == null) return currentY;
        for (String line : this.listLineBreakStringToWidth(text, wrapWidth)) {
            if (line.isEmpty()) {
                currentY += this.fontRenderer.FONT_HEIGHT;
                continue;
            }
            this.drawString(this.fontRenderer, line, x, y + currentY, color);
            currentY += this.fontRenderer.FONT_HEIGHT;
        }
        return currentY;
    }

    /**
     * Get a list of string lines from a raw string, which may contain "\n" or "\\n".
     * Can be seen as a better version of {@link net.minecraft.client.gui.FontRenderer#listFormattedStringToWidth(String, int)}.
     */
    public List<String> listLineBreakStringToWidth(@Nullable String text, int wrapWidth) {
        final List<String> lineList = new ArrayList<>();
        if (text == null) return lineList;
        text = text.replace("\\n", "\n");
        String[] paragraphs = text.split("\n", -1);
        for (String para : paragraphs) {
            if (para.isEmpty()) {
                lineList.add("");
                continue;
            }
            lineList.addAll(this.fontRenderer.listFormattedStringToWidth(para, wrapWidth));
        }
        return lineList;
    }

    /**
     * {@link KeyBinding#isActiveAndMatches(int)}，但是不检测 {@link KeyConflictContext#isActive()}。
     */
    public static boolean isKeyActiveIgnoreConflict(KeyBinding keyBinding, int keyCode) {
        return keyCode != 0 && keyCode == keyBinding.getKeyCode() && keyBinding.getKeyModifier().equals(KeyModifier.getActiveModifier());
    }
}
