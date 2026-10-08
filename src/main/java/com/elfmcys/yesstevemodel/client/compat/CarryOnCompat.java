package com.elfmcys.yesstevemodel.client.compat;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import tschipp.carryon.client.event.RenderEntityEvents;
import tschipp.carryon.client.event.RenderEvents;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.item.ItemEntity;
import tschipp.carryon.common.item.ItemTile;

import javax.annotation.Nullable;
import java.util.Locale;

public class CarryOnCompat {
    private static final String CARRY_ON = "carryon";
    private static boolean CARRY_ON_INSTALLED = false;

    public static void init() {
        CARRY_ON_INSTALLED = Loader.isModLoaded(CARRY_ON);
        if (CARRY_ON_INSTALLED) initEvents();
    }

    public static boolean isInstalled() {
        return CARRY_ON_INSTALLED;
    }

    @Nullable
    public static String getCarryOnString(EntityLivingBase player) {
        if (CARRY_ON_INSTALLED) {
            CarryOnType type = getCarryOnType(player.getHeldItemMainhand());
            if (type != CarryOnType.NONE) return type.name().toLowerCase(Locale.US);
        }
        return null;
    }

    @Optional.Method(modid = CARRY_ON)
    private static CarryOnType getCarryOnType(@Nullable ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            if (stack.getItem() == RegistrationHandler.itemTile && ItemTile.hasTileData(stack)) {
                return CarryOnType.BLOCK;
            }
            if (stack.getItem() == RegistrationHandler.itemEntity && ItemEntity.hasEntityData(stack)) {
                return CarryOnType.ENTITY;
            }
        }
        return CarryOnType.NONE;
    }

    public enum CarryOnType {
        ENTITY,
        BLOCK,
        NONE
    }

    private static RenderEvents BLOCK_EVENT;
    private static RenderEntityEvents ENTITY_EVENT;

    @Optional.Method(modid = CARRY_ON)
    private static void initEvents() {
        BLOCK_EVENT = new RenderEvents();
        ENTITY_EVENT = new RenderEntityEvents();
    }

    public static void renderCarryOn(EntityPlayer player, RenderPlayer renderer, float tick, double x, double y, double z) {
        if (CARRY_ON_INSTALLED) onRenderPlayer(new RenderPlayerEvent.Post(player, renderer, tick, x, y, z));
    }

    // FIXME：Carry On 自带的渲染存在诸多问题，先不修了
    @Optional.Method(modid = CARRY_ON)
    private static void onRenderPlayer(RenderPlayerEvent.Post event) {
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableColorMaterial();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        BLOCK_EVENT.onPlayerRenderPost(event);
        ENTITY_EVENT.onPlayerRenderPost(event);
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}
