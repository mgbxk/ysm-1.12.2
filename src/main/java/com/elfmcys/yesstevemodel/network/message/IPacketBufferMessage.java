package com.elfmcys.yesstevemodel.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public interface IPacketBufferMessage extends IMessage {
    void toBytes(PacketBuffer buf);

    void fromBytes(PacketBuffer buf);

    @Override
    default void toBytes(ByteBuf buf) {
        this.toBytes(new PacketBuffer(buf));
    }

    @Override
    default void fromBytes(ByteBuf buf) {
        this.fromBytes(new PacketBuffer(buf));
    }
}
