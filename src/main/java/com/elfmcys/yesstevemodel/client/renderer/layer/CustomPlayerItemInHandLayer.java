package com.elfmcys.yesstevemodel.client.renderer.layer;

import com.elfmcys.yesstevemodel.geckolib3.core.util.Color;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoLayerRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.IGeoRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;

import javax.annotation.Nonnull;

/**
 * 可参考原版实现 {@link net.minecraft.client.renderer.entity.layers.LayerHeldItem}。
 */
public class CustomPlayerItemInHandLayer<T extends EntityLivingBase, R extends IGeoRenderer<T>> extends GeoLayerRenderer<T, R> {
    //private final static String TAC_ID = "tac";

    public CustomPlayerItemInHandLayer(R entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(@Nonnull T entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, Color renderColor) {
        if (this.entityRenderer.getGeoModel() == null) {
            return;
        }
        ItemStack offhandItem = entityLivingBaseIn.getHeldItemOffhand();
        ItemStack mainHandItem = entityLivingBaseIn.getHeldItemMainhand();
        GeoModel geoModel = this.entityRenderer.getGeoModel();
        if (!offhandItem.isEmpty() || !mainHandItem.isEmpty()) {
            if (!geoModel.rightHandBones.isEmpty()) {
                GlStateManager.pushMatrix();
                this.renderArmWithItem(entityLivingBaseIn, mainHandItem, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, EnumHandSide.RIGHT);
                GlStateManager.popMatrix();
            }
            if (!geoModel.leftHandBones.isEmpty()) {
                GlStateManager.pushMatrix();
                this.renderArmWithItem(entityLivingBaseIn, offhandItem, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, EnumHandSide.LEFT);
                GlStateManager.popMatrix();
            }
//            if (Loader.isModLoaded(TAC_ID) && TacGunRenderer.isGun(offhandItem)) {
//                GlStateManager.pushMatrix();
//                TacGunRenderer.renderOffhandGun(offhandItem, geoModel, entityLivingBaseIn, packedLightIn, partialTicks);
//                GlStateManager.popMatrix();
//            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    protected void renderArmWithItem(EntityLivingBase livingEntity, ItemStack itemStack, ItemCameraTransforms.TransformType transformType, EnumHandSide arm) {
        if (!itemStack.isEmpty()) {
            boolean isLeftHand = arm == EnumHandSide.LEFT;
            // 缩放不为 0 才会渲染
            boolean scaleResult = translateToHand(arm, this.entityRenderer.getGeoModel());
            if (!scaleResult) {
//                if (Loader.isModLoaded(TAC_ID) && TacGunRenderer.isGun(itemStack)) {
//                    if (!isLeftHand) {
//                        TacGunRenderer.renderMainhandGun(itemStack, livingEntity, light, partialTicks);
//                    }
//                } else {
                GlStateManager.translate(0, -0.0625, -0.1);
                GlStateManager.rotate(-90.0F, 1, 0, 0);
                Minecraft.getMinecraft().getItemRenderer().renderItemSide(livingEntity, itemStack, transformType, isLeftHand);
//                }
            }
        }
    }

    protected static boolean translateToHand(EnumHandSide arm, GeoModel geoModel) {
        return RenderUtils.prepMatrixForLocator(arm == EnumHandSide.LEFT ? geoModel.leftHandBones : geoModel.rightHandBones);
    }
}
