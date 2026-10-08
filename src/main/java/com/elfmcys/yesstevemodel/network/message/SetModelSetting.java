package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.model.modern.ModernModelOptions;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Only declared settings of the sender's currently selected model may be changed. */
public final class SetModelSetting implements IPacketBufferMessage {
    private String model, variable; private double value;
    public SetModelSetting() {}
    public SetModelSetting(String model, String variable, double value) { this.model = model; this.variable = variable; this.value = value; }
    @Override public void toBytes(PacketBuffer buffer) { buffer.writeString(model); buffer.writeString(variable); buffer.writeDouble(value); }
    @Override public void fromBytes(PacketBuffer buffer) { model = buffer.readString(256); variable = buffer.readString(256); value = buffer.readDouble(); }
    public static final class Handler implements IMessageHandler<SetModelSetting, IMessage> {
        @Override public IMessage onMessage(SetModelSetting message, MessageContext context) {
            if (context.side.isServer()) FMLCommonHandler.instance().getWorldThread(context.netHandler).addScheduledTask(() ->
                CapabilityEvent.getModelInfoCap(context.getServerHandler().player).ifPresent(cap -> {
                    if (!cap.getModelId().toString().equals(message.model)) return;
                    ModernModelOptions options = ModernModelOptions.SERVER_MODELS.get(cap.getModelId().getPath());
                    ModernModelOptions.Form form = options == null ? null : options.settings.get(message.variable);
                    if (form == null || !Double.isFinite(message.value)) return;
                    try { form.apply(message.value, cap.getModelSettings()).forEach(cap::setModelSetting); }
                    catch (IllegalArgumentException | IllegalStateException invalid) { /* Reject invalid scripts without changing player settings. */ }
                }));
            return null;
        }
    }
}
