package dev.hipposgrumm.corrosive_sculk.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

// This is for instanceof checks.
public interface CorrosiveSculkPacket
    //? if >=1.20.5
    /*extends net.minecraft.network.protocol.common.custom.CustomPacketPayload*/
{
    void toBytes(FriendlyByteBuf buf);

    ResourceLocation getID();
}
