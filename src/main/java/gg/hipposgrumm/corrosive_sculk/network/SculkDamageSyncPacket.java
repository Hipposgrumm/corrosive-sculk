package gg.hipposgrumm.corrosive_sculk.network;

import com.mojang.logging.LogUtils;
import gg.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import gg.hipposgrumm.corrosive_sculk.sculk_damage_capability.SculkDamageCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SculkDamageSyncPacket {
    private final int damage;
    private final int protection;
    private final boolean sculkWarning;

    public SculkDamageSyncPacket(int damage, int protection, boolean sculkWarning) {
        this.damage = damage;
        this.protection = protection;
        this.sculkWarning = sculkWarning;
    }

    public SculkDamageSyncPacket(FriendlyByteBuf buf) {
        this.damage = buf.readInt();
        this.protection = buf.readInt();
        this.sculkWarning = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(damage);
        buf.writeInt(protection);
        buf.writeBoolean(sculkWarning);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            SculkDamageCapability.ClientData.set(damage, protection, sculkWarning);
        });
        return true;
    }
}