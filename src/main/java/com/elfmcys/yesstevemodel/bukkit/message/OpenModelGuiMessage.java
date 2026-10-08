package com.elfmcys.yesstevemodel.bukkit.message;

import com.elfmcys.yesstevemodel.client.gui.PlayerModelScreen;
import com.elfmcys.yesstevemodel.network.message.IPacketBufferMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class OpenModelGuiMessage implements IPacketBufferMessage {
    public static int CURRENT_NPC_ID = -1;
    private int entityId;
    private int npcId;

    public OpenModelGuiMessage() {
    }

    @Override
    public void toBytes(PacketBuffer buf) {
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.entityId = buf.readInt();
        this.npcId = buf.readInt();
    }

    public static class Handler implements IMessageHandler<OpenModelGuiMessage, IMessage> {
        @Override
        public IMessage onMessage(OpenModelGuiMessage message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                handleMessage(message);
            }
            return null;
        }

        @SideOnly(Side.CLIENT)
        private static void handleMessage(OpenModelGuiMessage message) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() -> {
                EntityPlayerSP localPlayer = mc.player;
                if (localPlayer != null) {
                    Entity entity = localPlayer.getEntityWorld().getEntityByID(message.entityId);
                    if (entity instanceof EntityPlayer player) {
                        CURRENT_NPC_ID = message.npcId;
                        mc.displayGuiScreen(new PlayerModelScreen(player));
                    }
                }
            });
        }
    }
}
