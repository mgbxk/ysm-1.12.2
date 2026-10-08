package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.command.sub.ManageCommand;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.List;

public class RefreshModelManage implements IMessage {
    public RefreshModelManage() {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<RefreshModelManage, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(RefreshModelManage message, MessageContext ctx) {
            if (ctx.side.isServer() && ctx.getServerHandler().player.canUseCommand(4, "")) {
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                    List<RequestServerModelInfo.Info> builtinInfo = ManageCommand.getFilesInfo(ServerModelManager.BUILTIN);
                    List<RequestServerModelInfo.Info> customInfo = ManageCommand.getFilesInfo(ServerModelManager.CUSTOM);
                    List<RequestServerModelInfo.Info> authInfo = ManageCommand.getFilesInfo(ServerModelManager.AUTH);
                    NetworkHandler.sendToClientPlayer(new RequestServerModelInfo(builtinInfo, customInfo, authInfo), ctx.getServerHandler().player);
                });
            }
            return null;
        }
    }
}
