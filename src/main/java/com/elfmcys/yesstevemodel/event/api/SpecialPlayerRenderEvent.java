package com.elfmcys.yesstevemodel.event.api;

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class SpecialPlayerRenderEvent extends Event {
    private final EntityPlayer player;
    private final CustomPlayerEntity customPlayer;
    private final ResourceLocation modelId;

    public SpecialPlayerRenderEvent(EntityPlayer player, CustomPlayerEntity customPlayer, ResourceLocation modelId) {
        this.player = player;
        this.customPlayer = customPlayer;
        this.modelId = modelId;
    }

    public EntityPlayer getPlayer() {
        return this.player;
    }

    public CustomPlayerEntity getCustomPlayer() {
        return this.customPlayer;
    }

    public ResourceLocation getModelId() {
        return this.modelId;
    }
}
