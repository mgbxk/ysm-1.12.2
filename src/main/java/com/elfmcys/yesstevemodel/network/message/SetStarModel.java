package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class SetStarModel implements IPacketBufferMessage {
    private ResourceLocation modelId;
    private boolean isAdd;

    public SetStarModel() {
    }

    private SetStarModel(ResourceLocation modelId, boolean isAdd) {
        this.modelId = modelId;
        this.isAdd = isAdd;
    }

    public static SetStarModel add(ResourceLocation modelId) {
        return new SetStarModel(modelId, true);
    }

    public static SetStarModel remove(ResourceLocation modelId) {
        return new SetStarModel(modelId, false);
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeResourceLocation(this.modelId);
        buf.writeBoolean(this.isAdd);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.modelId = buf.readResourceLocation();
        this.isAdd = buf.readBoolean();
    }

    public static class Handler implements IMessageHandler<SetStarModel, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(SetStarModel message, MessageContext ctx) {
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

        private static void handleCapability(SetStarModel message, EntityPlayerMP sender) {
            CapabilityEvent.getStarModelsCap(sender).ifPresent(cap -> {
                if (message.isAdd) {
                    cap.addModel(message.modelId);
                } else {
                    cap.removeModel(message.modelId);
                }
            });
        }
    }
}
