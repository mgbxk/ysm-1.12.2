package com.elfmcys.yesstevemodel.geckolib3.geo;

import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.util.Color;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.model.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData;
import com.elfmcys.yesstevemodel.geckolib3.util.EModelRenderCycle;
import com.elfmcys.yesstevemodel.geckolib3.util.IRenderCycle;
import com.elfmcys.yesstevemodel.mclib.utils.Interpolations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"rawtypes", "unchecked"})
public class GeoProjectilesRenderer<T extends Entity, E extends IAnimatable> extends Render<T> implements IGeoRenderer<T> {
    protected static final Map<Class<? extends IAnimatable>, GeoProjectilesRenderer> renderers = new ConcurrentHashMap<>();

    static {
        AnimationController.addModelFetcher((IAnimatable object) -> {
            GeoProjectilesRenderer renderer = renderers.get(object.getClass());
            return renderer == null ? null : renderer.getGeoModelProvider();
        });
    }

    protected final AnimatedGeoModel<IAnimatable> modelProvider;
    protected E animatable;
    private IRenderCycle currentModelRenderCycle = EModelRenderCycle.INITIAL;

    public GeoProjectilesRenderer(RenderManager renderManager, AnimatedGeoModel<IAnimatable> modelProvider, E animatable) {
        super(renderManager);
        this.modelProvider = modelProvider;
        this.animatable = animatable;
        renderers.putIfAbsent(animatable.getClass(), this);
    }

    @Override
    public void doRender(@Nonnull T entity, double x, double y, double z, float yaw, float partialTick) {
        GeoModel model = this.modelProvider.getModel(this.modelProvider.getModelLocation(this.animatable));
        this.setCurrentModelRenderCycle(EModelRenderCycle.INITIAL);
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(Interpolations.lerp(entity.prevRotationYaw, entity.rotationYaw, partialTick) - 90, 0, 1, 0);
        GlStateManager.rotate(Interpolations.lerp(entity.prevRotationPitch, entity.rotationPitch, partialTick), 0, 0, 1);

        /*
        参考 RenderArrow
         */
        // 箭矢不摇晃似乎是预期行为，先注释掉
        /*
        if (entity instanceof EntityArrow arrow) {
            float deltaShake = arrow.arrowShake - partialTick;
            if (deltaShake > 0.0F) {
                GlStateManager.rotate(-MathHelper.sin(deltaShake * 3.0F) * deltaShake, 0.0F, 0.0F, 1.0F);
            }
        }
         */

        AnimationEvent<E> predicate = new AnimationEvent<>(this.animatable, 0, 0, partialTick, false, Collections.singletonList(new EntityModelData()));
        this.modelProvider.setCustomAnimations(this.animatable, this.getInstanceId(entity), predicate);

        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.translate(0, 0.01f, 0);
        boolean scoreTeamColor = false;
        if (this.renderOutlines) {
            scoreTeamColor = this.setScoreTeamColor(entity);
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }

        Color renderColor = this.getRenderColor(entity, partialTick);
        boolean isVisible = this.isVisible(entity);
        boolean isGhost = !isVisible && !entity.isInvisibleToPlayer(mc.player);
        if ((isVisible || isGhost) && this.bindEntityTexture(entity)) {
            if (isGhost) GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
            this.render(model, entity, partialTick,
                    (float) renderColor.getRed() / 255f, (float) renderColor.getGreen() / 255f,
                    (float) renderColor.getBlue() / 255f, (float) renderColor.getAlpha() / 255);
            if (isGhost) GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
        }

        if (this.renderOutlines) {
            if (scoreTeamColor) this.unsetScoreTeamColor();
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, yaw, partialTick);
    }

    protected boolean isVisible(Entity livingEntityIn) {
        return !livingEntityIn.isInvisible() || this.renderOutlines;
    }

    /**
     * {@link net.minecraft.client.renderer.entity.RenderLivingBase#setScoreTeamColor(EntityLivingBase)}
     */
    @SuppressWarnings("JavadocReference")
    protected boolean setScoreTeamColor(Entity entityIn) {
        GlStateManager.disableLighting();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        return true;
    }

    /**
     * {@link net.minecraft.client.renderer.entity.RenderLivingBase#unsetScoreTeamColor()}
     */
    @SuppressWarnings("JavadocReference")
    protected void unsetScoreTeamColor() {
        GlStateManager.enableLighting();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    @Override
    public AnimatedGeoModel getGeoModelProvider() {
        return this.modelProvider;
    }

    @Override
    @Nonnull
    public IRenderCycle getCurrentModelRenderCycle() {
        return this.currentModelRenderCycle;
    }

    @Override
    public void setCurrentModelRenderCycle(IRenderCycle currentModelRenderCycle) {
        this.currentModelRenderCycle = currentModelRenderCycle;
    }

    @Nullable
    @Override
    public ResourceLocation getEntityTexture(@Nonnull T instance) {
        return this.modelProvider.getTextureLocation(this.animatable);
    }
}
