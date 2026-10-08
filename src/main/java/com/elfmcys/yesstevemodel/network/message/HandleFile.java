package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.model.ServerModelManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

public class HandleFile implements IPacketBufferMessage {
    private String name;
    private UploadFile.Dir dir;
    private String action;
    private String rename;

    public HandleFile() {
    }

    public HandleFile(String name, UploadFile.Dir dir, String action, String rename) {
        this.name = name;
        this.dir = dir;
        this.action = action;
        this.rename = rename;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeString(this.name);
        buf.writeEnumValue(this.dir);
        buf.writeString(this.action);
        buf.writeString(this.rename);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.name = buf.readString(Short.MAX_VALUE);
        this.dir = buf.readEnumValue(UploadFile.Dir.class);
        this.action = buf.readString(Short.MAX_VALUE);
        this.rename = buf.readString(Short.MAX_VALUE);
    }

    public static class Handler implements IMessageHandler<HandleFile, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(HandleFile message, MessageContext ctx) {
            if (ctx.side.isServer() && ctx.getServerHandler().player.canUseCommand(4, "")) {
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                    if (message.dir == UploadFile.Dir.CUSTOM) {
                        String actionIn = message.action;
                        File file = ServerModelManager.CUSTOM.resolve(message.name).toFile();
                        if (file.isFile() || file.isDirectory()) {
                            if (actionIn.equals("delete")) {
                                FileUtils.deleteQuietly(file);
                            }
                            if (actionIn.equals("move")) {
                                File destFile = ServerModelManager.AUTH.resolve(message.name).toFile();
                                try {
                                    if (file.isFile()) {
                                        FileUtils.moveFile(file, destFile);
                                    }
                                    if (file.isDirectory()) {
                                        FileUtils.moveDirectory(file, destFile);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (actionIn.equals("rename") && StringUtils.isNotBlank(message.rename)) {
                                File destFile = ServerModelManager.CUSTOM.resolve(message.rename).toFile();
                                try {
                                    if (file.isFile()) {
                                        FileUtils.moveFile(file, destFile);
                                    }
                                    if (file.isDirectory()) {
                                        FileUtils.moveDirectory(file, destFile);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    if (message.dir == UploadFile.Dir.AUTH) {
                        String actionIn = message.action;
                        File file = ServerModelManager.AUTH.resolve(message.name).toFile();
                        if (file.isFile() || file.isDirectory()) {
                            if (actionIn.equals("delete")) {
                                FileUtils.deleteQuietly(file);
                            }
                            if (actionIn.equals("move")) {
                                File destFile = ServerModelManager.CUSTOM.resolve(message.name).toFile();
                                try {
                                    if (file.isFile()) {
                                        FileUtils.moveFile(file, destFile);
                                    }
                                    if (file.isDirectory()) {
                                        FileUtils.moveDirectory(file, destFile);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (actionIn.equals("rename") && StringUtils.isNotBlank(message.rename)) {
                                File destFile = ServerModelManager.AUTH.resolve(message.rename).toFile();
                                try {
                                    if (file.isFile()) {
                                        FileUtils.moveFile(file, destFile);
                                    }
                                    if (file.isDirectory()) {
                                        FileUtils.moveDirectory(file, destFile);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }
            return null;
        }
    }
}
