package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.ClientProxy;
import com.elfmcys.yesstevemodel.client.config.GeneralConfig;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.renderer.CustomPlayerRenderer;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.event.api.SpecialPlayerRenderEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.mclib.utils.Interpolations;
import com.elfmcys.yesstevemodel.util.AnimatableCacheUtil;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.ExecutionException;

// TODO：测试
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = YesSteveModel.MOD_ID)
public class RenderFirstPlayerBackground {
    private static final String NAME = "Background";
    /**
     * 因为 RenderHandEvent 可有几率会渲染多次，所以为了避免多次渲染，这样设计
     */
    private static boolean ALREADY_RENDERED = false;

    @SubscribeEvent
    public static void onRenderLevelLase(RenderWorldLastEvent event) {
        ALREADY_RENDERED = false;
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (GeneralConfig.DISABLE_SELF_MODEL) {
            return;
        }
        if (GeneralConfig.DISABLE_SELF_HANDS) {
            return;
        }
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null || ALREADY_RENDERED) {
            return;
        }
        ALREADY_RENDERED = true;
        CapabilityEvent.getModelInfoCap(player).ifPresent(cap -> {
            ResourceLocation modelId = cap.getModelId();
            GeoModel geoModel = GeckoLibCache.getInstance().getGeoModels().get(ModelIdUtil.getArmId(cap.getModelId()));
            if (geoModel == null || !geoModel.hasTopLevelBone(NAME)) {
                return;
            }
            CustomPlayerRenderer instance = ClientProxy.getInstance();
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buffer = tess.getBuffer();
            IAnimatable animatable;

            try {
                animatable = AnimatableCacheUtil.ANIMATABLE_CACHE.get(modelId, () -> {
                    CustomPlayerEntity entity = new CustomPlayerEntity();
                    entity.setTexture(cap.getSelectTexture());
                    return entity;
                });
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }

            if (animatable instanceof CustomPlayerEntity customPlayer) {
                customPlayer.setTexture(cap.getSelectTexture());
                if (MinecraftForge.EVENT_BUS.post(new SpecialPlayerRenderEvent(player, customPlayer, modelId))) {
                    return;
                }
                buffer.begin(GL11.GL_QUADS, com.elfmcys.yesstevemodel.client.texture.OptifinePbr.vertexFormat(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL));
                if (instance != null) {
                    GlStateManager.pushMatrix();
                    if (Minecraft.getMinecraft().gameSettings.viewBobbing) {
                        bobView(event.getPartialTicks(), player);
                    }
                    GlStateManager.translate(0, -1.5, 0);
                    geoModel.getTopLevelBone(NAME).ifPresent(bone -> instance.renderRecursively(bone, tess, 1.0F, 1.0F, 1.0F, 1.0F));
                    tess.draw();
                    GlStateManager.popMatrix();
                }
            }
        });
    }

    private static void bobView(float pPartialTicks, EntityPlayerSP player) {
        float walk = player.distanceWalkedModified - player.prevDistanceWalkedModified;
        float walk2 = -(player.distanceWalkedModified + walk * pPartialTicks);
        float lerp = Interpolations.lerp(player.prevCameraYaw, player.cameraYaw, pPartialTicks);
        GlStateManager.translate(-MathHelper.sin(walk2 * (float) Math.PI) * lerp * 0.5F, Math.abs(MathHelper.cos(walk2 * (float) Math.PI) * lerp), 0.0D);
        GlStateManager.rotate(MathHelper.sin(walk2 * (float) Math.PI) * lerp * 3.0F, 0, 0, 1);
        GlStateManager.rotate(Math.abs(MathHelper.cos(walk2 * (float) Math.PI - 0.2F) * lerp) * 5.0F, 1, 0, 0);
    }
}
