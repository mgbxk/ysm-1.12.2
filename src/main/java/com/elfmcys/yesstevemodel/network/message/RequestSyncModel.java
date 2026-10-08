package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class RequestSyncModel implements IMessage {
    public RequestSyncModel() {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<RequestSyncModel, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(RequestSyncModel message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                handleClient();
            }
            return null;
        }

        @SideOnly(Side.CLIENT)
        private static void handleClient() {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                com.elfmcys.yesstevemodel.client.ClientSession.serverSyncRequested();
                ClientModelManager.sendSyncModelMessage();
            });
        }
    }
}
