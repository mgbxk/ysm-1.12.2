package com.elfmcys.yesstevemodel.bukkit.message;

import com.elfmcys.yesstevemodel.network.message.IPacketBufferMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SetNpcModelAndTexture implements IPacketBufferMessage {
    private ResourceLocation modelId;
    private ResourceLocation selectTexture;
    private int npcId;

    public SetNpcModelAndTexture() {
    }

    public SetNpcModelAndTexture(ResourceLocation modelId, ResourceLocation selectTexture, int npcId) {
        this.modelId = modelId;
        this.selectTexture = selectTexture;
        this.npcId = npcId;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeResourceLocation(this.modelId);
        buf.writeResourceLocation(this.selectTexture);
        buf.writeInt(this.npcId);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.modelId = buf.readResourceLocation();
        this.selectTexture = buf.readResourceLocation();
        this.npcId = buf.readInt();
    }

    public static class Handler implements IMessageHandler<SetNpcModelAndTexture, IMessage> {
        @Override
        public IMessage onMessage(SetNpcModelAndTexture message, MessageContext ctx) {
            return null;
        }
    }
}
