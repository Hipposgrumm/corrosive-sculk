package dev.hipposgrumm.corrosive_sculk.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

// This is for instanceof checks.
public interface CorrosiveSculkPacket {
    void toBytes(FriendlyByteBuf buf);

    //? if fabric
    /*ResourceLocation getID();*/
}
