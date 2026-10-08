package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.util.Md5Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class SendModelFile implements IPacketBufferMessage {
    private byte[] data;

    public SendModelFile() {
    }

    public SendModelFile(byte[] data) {
        this.data = data;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeByteArray(this.data);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.data = buf.readByteArray();
    }

    public static class Handler implements IMessageHandler<SendModelFile, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(SendModelFile message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                handleClient(message);
            }
            return null;
        }

        @SideOnly(Side.CLIENT)
        private static void handleClient(SendModelFile message) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                if (message.data.length == 48) {
                    ClientModelManager.PASSWORD = message.data;
                } else {
                    String fileName = Md5Utils.md5Hex(message.data).toUpperCase(Locale.US);
                    File file = ServerModelManager.CACHE_CLIENT.resolve(fileName).toFile();
                    try {
                        FileUtils.writeByteArrayToFile(file, message.data);
                        RequestLoadModel.loadModel(fileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
