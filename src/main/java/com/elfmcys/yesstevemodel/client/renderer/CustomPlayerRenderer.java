package com.elfmcys.yesstevemodel.client.renderer;

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.model.CustomPlayerModel;
import com.elfmcys.yesstevemodel.client.renderer.layer.CustomPlayerElytraLayer;
import com.elfmcys.yesstevemodel.client.renderer.layer.CustomPlayerItemInHandLayer;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.event.api.SpecialPlayerRenderEvent;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoReplacedEntityRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CustomPlayerRenderer extends GeoReplacedEntityRenderer<EntityPlayer, CustomPlayerEntity> {
    private GeoModel geoModel;

    @SuppressWarnings("all")
    public CustomPlayerRenderer(RenderManager ctx) {
        super(ctx, new CustomPlayerModel(), new CustomPlayerEntity());
        this.addLayer(new CustomPlayerItemInHandLayer<>(this));
        this.addLayer(new CustomPlayerElytraLayer<>(this));
    }

    @Override
    public void doRender(@Nonnull EntityPlayer entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (this.animatable != null) {
            CapabilityEvent.getModelInfoCap(entity).ifPresent(cap -> {
                this.animatable.setPlayer(entity);
                this.animatable.setMainModel(ModelIdUtil.getMainId(cap.getModelId()));
                this.animatable.setTexture(cap.getSelectTexture());
            });
            if (MinecraftForge.EVENT_BUS.post(new SpecialPlayerRenderEvent(entity, this.animatable, ModelIdUtil.getModelIdFromMainId(this.animatable.getMainModel())))) {
                return;
            }
        }
        ResourceLocation location = this.modelProvider.getModelLocation(this.animatable);
        GeoModel geoModel = GeckoLibCache.getInstance().getGeoModels().get(location);
        if (geoModel != null) {
            this.geoModel = geoModel;
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        }
    }

    @Override
    public void render(@Nonnull EntityPlayer player, CustomPlayerEntity target, double x, double y, double z, float yaw, float partialTicks) {
        GeoModel previousModel = this.geoModel;
        CustomPlayerEntity previousAnimatable = this.currentAnimatable;
        this.geoModel = GeckoLibCache.getInstance().getGeoModels().get(this.modelProvider.getModelLocation(target));
        try {
            super.render(player, target, x, y, z, yaw, partialTicks);
        } finally {
            this.geoModel = previousModel;
            this.currentAnimatable = previousAnimatable;
        }
    }

    @Override
    public float getWidthScale(EntityPlayer entity) {
        if (this.currentAnimatable != null) {
            return this.currentAnimatable.getWidthScale();
        }
        return super.getWidthScale(entity);
    }

    @Override
    public float getHeightScale(EntityPlayer entity) {
        if (this.currentAnimatable != null) {
            return this.currentAnimatable.getHeightScale();
        }
        return super.getHeightScale(entity);
    }

    public CustomPlayerEntity getCustomPlayerEntity() {
        return this.animatable;
    }

    @Override protected com.elfmcys.yesstevemodel.model.modern.ModernModelOptions getModernModelOptions() {
        com.elfmcys.yesstevemodel.client.animation.modern.ModernAssets.Bundle bundle = currentAnimatable == null ? null : com.elfmcys.yesstevemodel.client.animation.modern.ModernAssets.MODELS.get(currentAnimatable.getAnimation());
        return bundle == null ? super.getModernModelOptions() : bundle.options;
    }

    @Nullable
    public GeoModel getGeoModel() {
        return this.geoModel;
    }
}
