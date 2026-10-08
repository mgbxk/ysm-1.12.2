package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;

public class UploadFile implements IPacketBufferMessage {
    private String name;
    private byte[] fileBytes;
    private Dir dir;

    public UploadFile() {
    }

    public UploadFile(String name, byte[] fileBytes, Dir dir) {
        this.name = name;
        this.fileBytes = fileBytes;
        this.dir = dir;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeString(this.name);
        buf.writeByteArray(this.fileBytes);
        buf.writeEnumValue(this.dir);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.name = buf.readString(Short.MAX_VALUE);
        this.fileBytes = buf.readByteArray();
        this.dir = buf.readEnumValue(Dir.class);
    }

    public static class Handler implements IMessageHandler<UploadFile, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(UploadFile message, MessageContext ctx) {
            if (ctx.side.isServer() && ctx.getServerHandler().player.canUseCommand(4, "")) {
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                    writeFile(message, ctx.getServerHandler().player);
                });
            }
            return null;
        }

        private static void writeFile(UploadFile message, EntityPlayerMP player) {
            Path filePath;
            if (message.dir == Dir.CUSTOM) {
                filePath = ServerModelManager.CUSTOM.resolve(message.name);
            } else {
                filePath = ServerModelManager.AUTH.resolve(message.name);
            }
            try {
                FileUtils.writeByteArrayToFile(filePath.toFile(), message.fileBytes);
                NetworkHandler.sendToClientPlayer(new CompleteFeedback(), player);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public enum Dir {
        CUSTOM,
        AUTH
    }
}
