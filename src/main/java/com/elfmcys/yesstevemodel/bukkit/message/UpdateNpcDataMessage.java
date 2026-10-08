package com.elfmcys.yesstevemodel.bukkit.message;

import com.elfmcys.yesstevemodel.bukkit.client.NPCData;
import com.elfmcys.yesstevemodel.network.message.IPacketBufferMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

public class UpdateNpcDataMessage implements IPacketBufferMessage {
    private UUID uuid;
    private ResourceLocation modelId;
    private ResourceLocation textureId;

    public UpdateNpcDataMessage() {
    }

    @Override
    public void toBytes(PacketBuffer buf) {
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.uuid = buf.readUniqueId();
        this.modelId = new ResourceLocation(buf.readString(Short.MAX_VALUE));
        this.textureId = new ResourceLocation(buf.readString(Short.MAX_VALUE));
    }

    public static class Handler implements IMessageHandler<UpdateNpcDataMessage, IMessage> {
        @Override
        public IMessage onMessage(UpdateNpcDataMessage message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                handleMessage(message);
            }
            return null;
        }

        @SideOnly(Side.CLIENT)
        private static void handleMessage(UpdateNpcDataMessage message) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() -> {
                EntityPlayerSP localPlayer = mc.player;
                if (localPlayer != null) {
                    NPCData.put(message.uuid, message.modelId, message.textureId);
                }
            });
        }
    }
}
