package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class SetModelAndTexture implements IPacketBufferMessage {
    private ResourceLocation modelId;
    private ResourceLocation selectTexture;

    public SetModelAndTexture() {
    }

    public SetModelAndTexture(ResourceLocation modelId, ResourceLocation selectTexture) {
        this.modelId = modelId;
        this.selectTexture = selectTexture;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeResourceLocation(this.modelId);
        buf.writeResourceLocation(this.selectTexture);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.modelId = buf.readResourceLocation();
        this.selectTexture = buf.readResourceLocation();
    }

    public static class Handler implements IMessageHandler<SetModelAndTexture, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(SetModelAndTexture message, MessageContext ctx) {
            if (ctx.side.isServer()) {
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                    EntityPlayerMP sender = ctx.getServerHandler().player;
                    if (sender == null) {
                        return;
                    }
                    handleCapability(message, sender);
                });
            }
            return null;
        }

        private static void handleCapability(SetModelAndTexture message, EntityPlayerMP sender) {
            CapabilityEvent.getModelInfoCap(sender).ifPresent(modelIdCap -> CapabilityEvent.getAuthModelsCap(sender).ifPresent(ownModelsCap -> {
                if (!ServerModelManager.AUTH_MODELS.contains(message.modelId.getPath()) || ownModelsCap.containModel(message.modelId)) {
                    modelIdCap.setModelAndTexture(message.modelId, message.selectTexture);
                }
            }));
        }
    }
}
