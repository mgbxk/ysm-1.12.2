package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class SetPlayAnimation implements IPacketBufferMessage {
    private static final int STOP = -1;
    private int extraAnimationId;

    public SetPlayAnimation() {
    }

    public SetPlayAnimation(int extraAnimationId) {
        this.extraAnimationId = extraAnimationId;
    }

    public static SetPlayAnimation stop() {
        return new SetPlayAnimation(STOP);
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeInt(this.extraAnimationId);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.extraAnimationId = buf.readInt();
    }

    public static class Handler implements IMessageHandler<SetPlayAnimation, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(SetPlayAnimation message, MessageContext ctx) {
            if (ctx.side.isServer()) {
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                    EntityPlayerMP sender = ctx.getServerHandler().player;
                    if (sender == null) {
                        return;
                    }
                    if (STOP <= message.extraAnimationId && message.extraAnimationId < 8) {
                        handleCapability(message, sender);
                    }
                });
            }
            return null;
        }

        private static void handleCapability(SetPlayAnimation message, EntityPlayerMP sender) {
            CapabilityEvent.getModelInfoCap(sender).ifPresent(modelIdCap -> {
                if (message.extraAnimationId == STOP) {
                    modelIdCap.stopAnimation();
                } else {
                    modelIdCap.playAnimation("extra" + message.extraAnimationId);
                }
            });
        }
    }
}
