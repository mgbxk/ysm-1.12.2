package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Set;

public class SyncStarModels implements IPacketBufferMessage {
    private Set<ResourceLocation> starModels;

    public SyncStarModels() {
    }

    public SyncStarModels(Set<ResourceLocation> starModels) {
        this.starModels = starModels;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeVarInt(this.starModels.size());
        for (ResourceLocation modelId : this.starModels) {
            buf.writeResourceLocation(modelId);
        }
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        int size = buf.readVarInt();
        Set<ResourceLocation> tmp = Sets.newHashSet();
        for (int i = 0; i < size; i++) {
            tmp.add(buf.readResourceLocation());
        }
        this.starModels = tmp;
    }

    public static class Handler implements IMessageHandler<SyncStarModels, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(SyncStarModels message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                handleCapability(message);
            }
            return null;
        }

        @SideOnly(Side.CLIENT)
        private static void handleCapability(SyncStarModels message) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() -> {
                if (mc.player != null) {
                    CapabilityEvent.getStarModelsCap(mc.player).ifPresent(cap -> cap.setStarModels(message.starModels));
                }
            });
        }
    }
}
