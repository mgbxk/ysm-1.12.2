package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.data.EncryptTools;
import com.elfmcys.yesstevemodel.data.ModelData;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.util.ThreadTools;
import com.elfmcys.yesstevemodel.util.UuidUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.UUID;

public class RequestLoadModel implements IPacketBufferMessage {
    private String fileName;

    public RequestLoadModel() {
    }

    public RequestLoadModel(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeString(this.fileName);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.fileName = buf.readString(Short.MAX_VALUE);
    }

    public static class Handler implements IMessageHandler<RequestLoadModel, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(RequestLoadModel message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                handleClient(message);
            }
            return null;
        }

        @SideOnly(Side.CLIENT)
        private static void handleClient(RequestLoadModel message) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                ClientModelManager.CACHE_MD5.add(message.fileName);
                loadModel(message.fileName);
            });
        }
    }

    @SideOnly(Side.CLIENT)
    public static void loadModel(String fileName) {
        final long generation = ClientModelManager.generation;
        ThreadTools.THREAD_POOL.submit(() -> {
            try {
                while (ClientModelManager.PASSWORD == null) {
                    if (generation != ClientModelManager.generation || !com.elfmcys.yesstevemodel.client.ClientSession.isSynced()) return;
                    Thread.sleep(500);
                }
                if (generation == ClientModelManager.generation && Minecraft.getMinecraft().player != null) {
                    UUID uuid = Minecraft.getMinecraft().player.getUniqueID();
                    Path modelFile = ServerModelManager.CACHE_CLIENT.resolve(fileName);
                    byte[] fileBytes = FileUtils.readFileToByteArray(modelFile.toFile());
                    ModelData data = EncryptTools.decryptModel(UuidUtils.asBytes(uuid), ClientModelManager.PASSWORD, fileBytes);
                    if (data != null) {
                        Minecraft.getMinecraft().addScheduledTask(() -> {
                            if (generation == ClientModelManager.generation && com.elfmcys.yesstevemodel.client.ClientSession.isSynced()) ClientModelManager.registerAll(data);
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
