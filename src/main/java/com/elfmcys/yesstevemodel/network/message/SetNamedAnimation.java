package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.model.modern.ModernModelOptions;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class SetNamedAnimation implements IPacketBufferMessage {
    private String model, animation;
    public SetNamedAnimation() {}
    public SetNamedAnimation(String model, String animation) { this.model = model; this.animation = animation; }
    @Override public void toBytes(PacketBuffer buffer) { buffer.writeString(model); buffer.writeString(animation); }
    @Override public void fromBytes(PacketBuffer buffer) { model = buffer.readString(256); animation = buffer.readString(256); }
    public static final class Handler implements IMessageHandler<SetNamedAnimation, IMessage> {
        @Override public IMessage onMessage(SetNamedAnimation message, MessageContext context) {
            if (context.side.isServer()) FMLCommonHandler.instance().getWorldThread(context.netHandler).addScheduledTask(() ->
                CapabilityEvent.getModelInfoCap(context.getServerHandler().player).ifPresent(cap -> {
                    if (!cap.getModelId().toString().equals(message.model)) return;
                    ModernModelOptions options = ModernModelOptions.SERVER_MODELS.get(cap.getModelId().getPath());
                    if (options != null && options.playable.contains(message.animation)) cap.playAnimation(message.animation);
                }));
            return null;
        }
    }
}
