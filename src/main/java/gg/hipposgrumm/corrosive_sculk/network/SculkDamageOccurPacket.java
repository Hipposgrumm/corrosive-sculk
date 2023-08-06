package gg.hipposgrumm.corrosive_sculk.network;

import gg.hipposgrumm.corrosive_sculk.sculk_damage_capability.SculkDamageCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SculkDamageOccurPacket {
    private final boolean isHeal;

    public SculkDamageOccurPacket(boolean isHeal) {
            this.isHeal = isHeal;
    }

    public SculkDamageOccurPacket(FriendlyByteBuf buf) {
            this.isHeal = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
            buf.writeBoolean(isHeal);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            SculkDamageCapability.ClientData.playHeartSound(isHeal);
        });
        return true;
    }
}