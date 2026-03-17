package dev.hipposgrumm.corrosive_sculk.network;

import dev.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import dev.hipposgrumm.corrosive_sculk.capability.SculkDamageCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SculkDamageSoundPacket implements CorrosiveSculkPacket {
    private final Type type;

    public SculkDamageSoundPacket(Type type) {
        this.type = type;
    }

    public SculkDamageSoundPacket(FriendlyByteBuf buf) {
        this.type = buf.readEnum(Type.class);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(type);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            SculkDamageCapability.ClientData.playHeartSound((switch (type) {
                case DAMAGE -> CorrosiveSculk.HEART_SCULK;
                case HEAL -> CorrosiveSculk.HEART_SCULK_RETURN;
                case WARN -> CorrosiveSculk.HEART_SCULK_WARN;
            }).get());
        });
    }

    public enum Type {
        DAMAGE,
        HEAL,
        WARN
    }
}