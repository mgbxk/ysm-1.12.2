package com.elfmcys.yesstevemodel.client.renderer;

import com.elfmcys.yesstevemodel.client.entity.CustomArrowEntity;
import com.elfmcys.yesstevemodel.client.model.CustomArrowModel;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoProjectilesRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.projectile.EntityArrow;

import javax.annotation.Nonnull;

public class CustomArrowRenderer extends GeoProjectilesRenderer<EntityArrow, CustomArrowEntity> {
    @SuppressWarnings("unchecked")
    public CustomArrowRenderer(RenderManager ctx) {
        super(ctx, new CustomArrowModel(), new CustomArrowEntity());
    }

    @Override
    public void doRender(@Nonnull EntityArrow entity, double x, double y, double z, float entityYaw, float partialTick) {
        if (this.animatable != null) {
            this.animatable.setArrow(entity);
        }
        super.doRender(entity, x, y, z, entityYaw, partialTick);
    }

    @Override
    public float getWidthScale(EntityArrow entity) {
        return 0.7f;
    }

    @Override
    public float getHeightScale(EntityArrow entity) {
        return 0.7f;
    }
}
