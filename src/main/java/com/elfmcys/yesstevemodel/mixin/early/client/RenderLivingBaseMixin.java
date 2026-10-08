package com.elfmcys.yesstevemodel.mixin.early.client;

import com.elfmcys.yesstevemodel.geckolib3.extended.RenderLivingBaseAccessor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RenderLivingBase.class)
public abstract class RenderLivingBaseMixin extends Render<EntityLivingBase> implements RenderLivingBaseAccessor {
    protected RenderLivingBaseMixin(RenderManager pContext) {
        super(pContext);
    }

    @Unique
    @Override
    public void ysm$renderNameTag(EntityLivingBase entity, double x, double y, double z, float entityYaw, float partialTick) {
        super.doRender(entity, x, y, z, entityYaw, partialTick);
    }
}
