package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.capability.ModelInfoCapability;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.util.ThreadTools;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.io.IOException;

public class SyncModelInfo implements IPacketBufferMessage {
    private int entityId;
    private ModelInfoCapability capability;

    public SyncModelInfo() {
    }

    public SyncModelInfo(int entityId, ModelInfoCapability capability) {
        this.entityId = entityId;
        this.capability = capability;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeVarInt(this.entityId);
        buf.writeCompoundTag(this.capability.serializeNBT());
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.entityId = buf.readVarInt();
        try {
            NBTTagCompound compoundTag = buf.readCompoundTag();
            ModelInfoCapability cap = new ModelInfoCapability();
            if (compoundTag != null) {
                cap.deserializeNBT(compoundTag);
            }
            this.capability = cap;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Handler implements IMessageHandler<SyncModelInfo, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(SyncModelInfo message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                handleCapability(message);
            }
            return null;
        }

        @SideOnly(Side.CLIENT)
        private static void handleCapability(SyncModelInfo message) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() -> {
                if (mc.world != null) {
                    ThreadTools.THREAD_POOL.submit(() -> {
                        try {
                            int time = 0;
                            while (mc.world.getEntityByID(message.entityId) == null && time < 5) {
                                Thread.sleep(500);
                                time++;
                            }
                            Entity entity = mc.world.getEntityByID(message.entityId);
                            if (entity instanceof EntityPlayer player) {
                                CapabilityEvent.getModelInfoCap(player).ifPresent(cap -> cap.copyFrom(message.capability));
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            });
        }
    }
}
