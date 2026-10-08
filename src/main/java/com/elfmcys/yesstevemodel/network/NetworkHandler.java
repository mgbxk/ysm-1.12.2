package com.elfmcys.yesstevemodel.network;

import com.elfmcys.yesstevemodel.bukkit.message.OpenModelGuiMessage;
import com.elfmcys.yesstevemodel.bukkit.message.SetNpcModelAndTexture;
import com.elfmcys.yesstevemodel.bukkit.message.SyncNpcDataMessage;
import com.elfmcys.yesstevemodel.bukkit.message.UpdateNpcDataMessage;
import com.elfmcys.yesstevemodel.network.message.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;


public final class NetworkHandler {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("yesstevemodel");
    public static final int OPEN_NPC_MODEL_GUI = 93;
    public static final int SET_NPC_MODEL_ID = 94;
    public static final int SYNC_NPC_DATA = 95;
    public static final int UPDATE_NPC_DATA = 96;

    public static void init() {
        // 最后一个参数是接收端
        CHANNEL.registerMessage(SyncModelFiles.Handler.class, SyncModelFiles.class, 0, Side.SERVER);
        CHANNEL.registerMessage(SendModelFile.Handler.class, SendModelFile.class, 1, Side.CLIENT);
        CHANNEL.registerMessage(RequestSyncModel.Handler.class, RequestSyncModel.class, 2, Side.CLIENT);
        CHANNEL.registerMessage(RequestLoadModel.Handler.class, RequestLoadModel.class, 3, Side.CLIENT);
        CHANNEL.registerMessage(SyncModelInfo.Handler.class, SyncModelInfo.class, 4, Side.CLIENT);
        CHANNEL.registerMessage(SetModelAndTexture.Handler.class, SetModelAndTexture.class, 5, Side.SERVER);
        CHANNEL.registerMessage(SyncAuthModels.Handler.class, SyncAuthModels.class, 6, Side.CLIENT);
        CHANNEL.registerMessage(SetPlayAnimation.Handler.class, SetPlayAnimation.class, 7, Side.SERVER);
        CHANNEL.registerMessage(SyncStarModels.Handler.class, SyncStarModels.class, 8, Side.CLIENT);
        CHANNEL.registerMessage(SetStarModel.Handler.class, SetStarModel.class, 9, Side.SERVER);
        CHANNEL.registerMessage(RequestServerModelInfo.Handler.class, RequestServerModelInfo.class, 10, Side.CLIENT);
        CHANNEL.registerMessage(UploadFile.Handler.class, UploadFile.class, 11, Side.SERVER);
        CHANNEL.registerMessage(CompleteFeedback.Handler.class, CompleteFeedback.class, 12, Side.CLIENT);
        CHANNEL.registerMessage(RefreshModelManage.Handler.class, RefreshModelManage.class, 13, Side.SERVER);
        CHANNEL.registerMessage(HandleFile.Handler.class, HandleFile.class, 14, Side.SERVER);
        CHANNEL.registerMessage(SyncArrowModel.Handler.class, SyncArrowModel.class, 15, Side.CLIENT);
        CHANNEL.registerMessage(SetModelSetting.Handler.class, SetModelSetting.class, 16, Side.SERVER);
        CHANNEL.registerMessage(SetNamedAnimation.Handler.class, SetNamedAnimation.class, 17, Side.SERVER);
        initBukkit();
    }

    private static void initBukkit() {
        CHANNEL.registerMessage(OpenModelGuiMessage.Handler.class, OpenModelGuiMessage.class, OPEN_NPC_MODEL_GUI, Side.CLIENT);
        CHANNEL.registerMessage(SetNpcModelAndTexture.Handler.class, SetNpcModelAndTexture.class, SET_NPC_MODEL_ID, Side.SERVER);
        CHANNEL.registerMessage(SyncNpcDataMessage.Handler.class, SyncNpcDataMessage.class, SYNC_NPC_DATA, Side.CLIENT);
        CHANNEL.registerMessage(UpdateNpcDataMessage.Handler.class, UpdateNpcDataMessage.class, UPDATE_NPC_DATA, Side.CLIENT);
    }

    public static void sendToClientPlayer(IMessage message, EntityPlayer player) {
        if (player instanceof EntityPlayerMP playerMP) {
            CHANNEL.sendTo(message, playerMP);
        }
    }

}
