package com.elfmcys.yesstevemodel.client.renderer.layer;

import com.elfmcys.yesstevemodel.client.compat.ElytraCompat;
import com.elfmcys.yesstevemodel.geckolib3.core.util.Color;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoLayerRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.IGeoRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelElytra;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * 可参考原版实现 {@link net.minecraft.client.renderer.entity.layers.LayerElytra}。
 */
public class CustomPlayerElytraLayer<T extends EntityLivingBase, R extends IGeoRenderer<T>> extends GeoLayerRenderer<T, R> {
    private final ModelElytra elytraModel = new ModelElytra();

    public CustomPlayerElytraLayer(R entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(@Nonnull T livingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, Color renderColor) {
        if (ElytraCompat.isWearingElytra(livingEntity) && this.entityRenderer.getGeoModel() != null) {
            GeoModel geoModel = this.entityRenderer.getGeoModel();
            if (!geoModel.elytraBones.isEmpty()) {
                ResourceLocation texture = ElytraCompat.getDefaultTexture(livingEntity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem());
                if (livingEntity instanceof AbstractClientPlayer player) {
                    if (player.isPlayerInfoSet() && player.getLocationElytra() != null) {
                        texture = player.getLocationElytra();
                    } else if (player.hasPlayerInfo() && player.getLocationCape() != null && player.isWearing(EnumPlayerModelParts.CAPE)) {
                        texture = player.getLocationCape();
                    }
                }
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

                GlStateManager.pushMatrix();
                boolean scaleResult = translateToElytra(geoModel);
                // 缩放不为 0 才会渲染
                if (!scaleResult) {
                    GlStateManager.rotate(180, 0, 0, 1);
                    Minecraft mc = Minecraft.getMinecraft();
                    mc.getTextureManager().bindTexture(texture);
                    final float scale = 1 / 16F;
                    this.elytraModel.setRotationAngles(pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch, scale, livingEntity);
                    this.elytraModel.render(livingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch, scale);
                    ItemStack stack = livingEntity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                    if (stack.hasEffect()) {
                        RenderPlayer renderer = mc.getRenderManager().getSkinMap().get("default");
                        LayerArmorBase.renderEnchantedGlint(renderer, livingEntity, this.elytraModel, pLimbSwing, pLimbSwingAmount, pPartialTicks, pAgeInTicks, pNetHeadYaw, pHeadPitch, scale);
                    }
                }
                GlStateManager.popMatrix();

                GlStateManager.disableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    protected static boolean translateToElytra(GeoModel geoModel) {
        return RenderUtils.prepMatrixForLocator(geoModel.elytraBones);
    }
}
