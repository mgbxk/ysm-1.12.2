package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.client.upload.UploadManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class CompleteFeedback implements IMessage {
    public CompleteFeedback() {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<CompleteFeedback, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(CompleteFeedback message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                handleClient();
            }
            return null;
        }

        @SideOnly(Side.CLIENT)
        private static void handleClient() {
            Minecraft.getMinecraft().addScheduledTask(UploadManager::finishUpload);
        }
    }
}
