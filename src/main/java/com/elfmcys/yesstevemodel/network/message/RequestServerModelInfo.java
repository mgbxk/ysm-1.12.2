package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.client.gui.ModelManageScreen;
import com.elfmcys.yesstevemodel.model.format.Type;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class RequestServerModelInfo implements IPacketBufferMessage {
    private List<Info> builtinModels;
    private List<Info> customModels;
    private List<Info> authModels;

    public RequestServerModelInfo() {
    }

    public RequestServerModelInfo(List<Info> builtinModels, List<Info> customModels, List<Info> authModels) {
        this.builtinModels = builtinModels;
        this.customModels = customModels;
        this.authModels = authModels;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeVarInt(this.builtinModels.size());
        for (Info info : this.builtinModels) {
            infoToBuffer(buf, info);
        }
        buf.writeVarInt(this.customModels.size());
        for (Info info : this.customModels) {
            infoToBuffer(buf, info);
        }
        buf.writeVarInt(this.authModels.size());
        for (Info info : this.authModels) {
            infoToBuffer(buf, info);
        }
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.builtinModels = Lists.newArrayList();
        int builtinSize = buf.readVarInt();
        for (int i = 0; i < builtinSize; i++) {
            this.builtinModels.add(bufferToInfo(buf));
        }
        this.customModels = Lists.newArrayList();
        int customSize = buf.readVarInt();
        for (int i = 0; i < customSize; i++) {
            this.customModels.add(bufferToInfo(buf));
        }
        this.authModels = Lists.newArrayList();
        int authSize = buf.readVarInt();
        for (int i = 0; i < authSize; i++) {
            this.authModels.add(bufferToInfo(buf));
        }
    }

    public static class Handler implements IMessageHandler<RequestServerModelInfo, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(RequestServerModelInfo message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                handleClient(message);
            }
            return null;
        }

        @SideOnly(Side.CLIENT)
        private static void handleClient(RequestServerModelInfo message) {
            Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(new ModelManageScreen(message.customModels, message.authModels)));
        }
    }

    private static void infoToBuffer(PacketBuffer buf, Info info) {
        buf.writeString(info.fileName);
        buf.writeEnumValue(info.type);
        buf.writeLong(info.size);
    }

    private static Info bufferToInfo(PacketBuffer buf) {
        return new Info(buf.readString(Short.MAX_VALUE), buf.readEnumValue(Type.class), buf.readLong());
    }

    public static class Info {
        private String fileName;
        private Type type;
        private long size;

        public Info(String fileName, Type type, long size) {
            this.fileName = fileName;
            this.type = type;
            this.size = size;
        }

        public String getFileName() {
            return this.fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public Type getType() {
            return this.type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public long getSize() {
            return this.size;
        }

        public void setSize(long size) {
            this.size = size;
        }
    }
}
