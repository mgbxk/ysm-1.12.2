package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.data.EncryptTools;
import com.elfmcys.yesstevemodel.model.format.ServerModelInfo;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.util.ThreadTools;
import com.elfmcys.yesstevemodel.util.UuidUtils;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.elfmcys.yesstevemodel.model.ServerModelManager.*;

public class SyncModelFiles implements IPacketBufferMessage {
    private String[] md5Info;

    public SyncModelFiles() {
    }

    public SyncModelFiles(String[] md5Info) {
        this.md5Info = md5Info;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeVarInt(this.md5Info.length);
        for (String md5 : this.md5Info) {
            buf.writeString(md5);
        }
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        int count = buf.readVarInt();
        String[] output = new String[count];
        for (int i = 0; i < count; i++) {
            output[i] = buf.readString(Short.MAX_VALUE);
        }
        this.md5Info = output;
    }

    public static class Handler implements IMessageHandler<SyncModelFiles, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(SyncModelFiles message, MessageContext ctx) {
            if (ctx.side.isServer()) {
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                    EntityPlayerMP sender = ctx.getServerHandler().player;
                    if (sender == null) {
                        return;
                    }
                    sendPassword(sender);
                    sendModelFiles(message.md5Info, sender);
                });
            }
            return null;
        }

        private static void sendModelFiles(String[] md5Info, EntityPlayerMP sender) {
            Collection<String> cache = CACHE_NAME_INFO.values().stream().map(ServerModelInfo::getMd5).collect(Collectors.toList());
            List<String> output = Lists.newArrayList(cache);
            for (String md5 : md5Info) {
                if (cache.contains(md5)) {
                    output.remove(md5);
                    NetworkHandler.sendToClientPlayer(new RequestLoadModel(md5), sender);
                }
            }
            for (String md5 : output) {
                File modelFile = CACHE_SERVER.resolve(md5).toFile();
                try {
                    byte[] modelBytes = FileUtils.readFileToByteArray(modelFile);
                    ThreadTools.THREAD_POOL.submit(() -> NetworkHandler.sendToClientPlayer(new SendModelFile(modelBytes), sender));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private static void sendPassword(EntityPlayerMP sender) {
            try {
                byte[] password = FileUtils.readFileToByteArray(PASSWORD_FILE.toFile());
                byte[] uuid = UuidUtils.asBytes(sender.getUniqueID());
                byte[] output = EncryptTools.encryptPassword(uuid, password);
                ThreadTools.THREAD_POOL.submit(() -> NetworkHandler.sendToClientPlayer(new SendModelFile(output), sender));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
