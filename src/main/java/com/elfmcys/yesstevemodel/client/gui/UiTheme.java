package com.elfmcys.yesstevemodel.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/** Small, static GUI primitives; no framebuffer effects or per-frame texture creation. */
public final class UiTheme extends Gui {
    public static final int TEXT = 0xE6EDF7, MUTED = 0x91A3BA, ACCENT = 0x7AE1C4;
    public static final int SHELL = 0xF5101926, PANEL = 0xEF162231, CARD = 0xF01C2A3B;
    public static final int BORDER = 0xFF34465A, SELECTED = 0xEF25483F;
    private static final UiTheme DRAW = new UiTheme();
    private static final double[][] ARC = new double[6][2];
    static { for (int i = 0; i < ARC.length; i++) { ARC[i][0] = Math.cos(Math.PI * i / 10); ARC[i][1] = Math.sin(Math.PI * i / 10); } }
    private UiTheme() {}
    public static void backdrop(int width, int height) {
        // In-world menus are overlays: leave the world and HUD outside panels untouched.
        if (Minecraft.getMinecraft().world == null) {
            DRAW.drawGradientRect(0, 0, width, height, 0xFF182536, 0xFF0B1420);
        }
    }
    public static void panel(int x, int y, int width, int height, int color, int border) {
        rounded(x, y, width, height, 7, border);
        rounded(x + 1, y + 1, width - 2, height - 2, 6, color);
    }
    public static void shell(int x, int y, int width, int height) {
        rounded(x - 3, y + 3, width + 6, height + 2, 10, 0x32000000);
        panel(x, y, width, height, SHELL, BORDER);
    }
    public static void rounded(int x, int y, int width, int height, int radius, int color) {
        if (width <= 0 || height <= 0) return;
        double r = Math.min(radius, Math.min(width, height) / 2d);
        float a = (color >>> 24) / 255f, red = (color >> 16 & 255) / 255f, green = (color >> 8 & 255) / 255f, blue = (color & 255) / 255f;
        GlStateManager.enableBlend(); GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        b.pos(x + width / 2d, y + height / 2d, 0).color(red, green, blue, a).endVertex();
        for (int corner = 0; corner < 4; corner++) for (int i = 0; i < ARC.length; i++) {
            double c = ARC[i][0], s = ARC[i][1], px, py;
            if (corner == 0) { px = x + r - r * s; py = y + r - r * c; }
            else if (corner == 1) { px = x + r - r * c; py = y + height - r + r * s; }
            else if (corner == 2) { px = x + width - r + r * s; py = y + height - r + r * c; }
            else { px = x + width - r + r * c; py = y + r - r * s; }
            b.pos(px, py, 0).color(red, green, blue, a).endVertex();
        }
        b.pos(x + r, y, 0).color(red, green, blue, a).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.enableTexture2D(); GlStateManager.disableBlend(); GlStateManager.color(1, 1, 1, 1);
    }
    public static void text(FontRenderer font, String text, int x, int y, int color) { font.drawString(text, x, y, color); }
    public static void centered(FontRenderer font, String text, int x, int y, int color) { text(font, text, x - font.getStringWidth(text) / 2, y, color); }
    public static void scaled(FontRenderer font, String text, int x, int y, float scale, int color) {
        GlStateManager.pushMatrix(); GlStateManager.translate(x, y, 0); GlStateManager.scale(scale, scale, 1);
        font.drawString(text, 0, 0, color); GlStateManager.popMatrix();
    }
    public static String fit(FontRenderer font, String text, int width) {
        if (font.getStringWidth(text) <= width) return text;
        return font.trimStringToWidth(text, Math.max(0, width - font.getStringWidth("..."))) + "...";
    }
    public static String modelLabel(String text) {
        return text.replaceFirst("^#", "").replaceFirst("^((?:\u00a7[0-9a-fk-orA-FK-OR])*)#", "$1")
                .replaceFirst("^extra([0-9]+)$", "动作 $1");
    }
    public static void pill(FontRenderer font, String text, int x, int y, boolean active) {
        int width = font.getStringWidth(text) + 14;
        rounded(x, y, width, 17, 8, active ? 0xFF25483F : CARD);
        text(font, text, x + 7, y + 5, active ? ACCENT : MUTED);
    }
}
