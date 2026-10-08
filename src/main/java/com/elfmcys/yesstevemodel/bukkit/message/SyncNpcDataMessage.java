package com.elfmcys.yesstevemodel.bukkit.message;

import com.elfmcys.yesstevemodel.bukkit.client.NPCData;
import com.elfmcys.yesstevemodel.network.message.IPacketBufferMessage;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.UUID;

public class SyncNpcDataMessage implements IPacketBufferMessage {
    private Map<UUID, Pair<ResourceLocation, ResourceLocation>> data;

    public SyncNpcDataMessage() {
    }

    @Override
    public void toBytes(PacketBuffer buf) {
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        Map<UUID, Pair<ResourceLocation, ResourceLocation>> map = Maps.newHashMap();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            UUID uuid = buf.readUniqueId();
            ResourceLocation modelId = new ResourceLocation(buf.readString(Short.MAX_VALUE));
            ResourceLocation textureId = new ResourceLocation(buf.readString(Short.MAX_VALUE));
            map.put(uuid, Pair.of(modelId, textureId));
        }
        this.data = map;
    }

    public static class Handler implements IMessageHandler<SyncNpcDataMessage, IMessage> {
        @Override
        public IMessage onMessage(SyncNpcDataMessage message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                handleMessage(message);
            }
            return null;
        }

        @SideOnly(Side.CLIENT)
        private static void handleMessage(SyncNpcDataMessage message) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() -> {
                EntityPlayerSP localPlayer = mc.player;
                if (localPlayer != null) {
                    NPCData.addAll(message.data);
                }
            });
        }
    }
}
