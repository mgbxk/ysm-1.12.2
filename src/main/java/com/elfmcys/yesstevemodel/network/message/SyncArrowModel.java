package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class SyncArrowModel implements IPacketBufferMessage {
    private int entityId;
    private String modelId;

    public SyncArrowModel() {
    }

    public SyncArrowModel(int entityId, String modelId) {
        this.entityId = entityId;
        this.modelId = modelId;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeVarInt(this.entityId);
        buf.writeString(this.modelId);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.entityId = buf.readVarInt();
        this.modelId = buf.readString(Short.MAX_VALUE);
    }

    public static class Handler implements IMessageHandler<SyncArrowModel, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(SyncArrowModel message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                handleCapability(message);
            }
            return null;
        }

        @SideOnly(Side.CLIENT)
        private static void handleCapability(SyncArrowModel message) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() -> {
                if (mc.world != null && mc.world.getEntityByID(message.entityId) instanceof EntityArrow arrow) {
                    CapabilityEvent.getArrowModelCap(arrow).ifPresent(cap -> cap.setModelId(message.modelId));
                }
            });
        }
    }
}
