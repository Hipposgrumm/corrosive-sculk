package dev.hipposgrumm.corrosive_sculk.network;

import dev.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import dev.hipposgrumm.corrosive_sculk.capability.SculkDamageCapability;
import dev.hipposgrumm.corrosive_sculk.config.Config;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SculkDamageSyncPacket implements CorrosiveSculkPacket {
    private final int entity;

    private final Type type;
    private final Integer damage;
    private final Integer protection;
    private final Integer maxProtection;
    private final Byte sculkWarningPercent;

    private SculkDamageSyncPacket(Entity entity, SculkDamageCapability data) {
        this.entity = entity.getId();
        this.type = Type.DAMAGE;
        this.damage = data.getDamage();
        if (Config.sculkResistInvul) {
            if (entity instanceof LivingEntity en && en.hasEffect(CorrosiveSculk.SCULK_RESISTANCE.get())) {
                this.protection = 10; // good enough(TM)
                this.maxProtection = 10;
            } else {
                this.protection = 0;
                this.maxProtection = 0;
            }
        } else {
            this.protection = data.getProtection();
            this.maxProtection = data.getMaxProtection();
        }
        this.sculkWarningPercent = data.getWarning();
    }

    private SculkDamageSyncPacket(Entity entity, byte warning) {
        this.entity = entity.getId();
        this.type = Type.WARNING;
        this.damage = null;
        this.protection = null;
        this.maxProtection = null;
        this.sculkWarningPercent = warning;
    }

    private SculkDamageSyncPacket(Entity entity) {
        this.entity = entity.getId();
        this.type = Type.REMOVE;
        this.damage = null;
        this.protection = null;
        this.maxProtection = null;
        this.sculkWarningPercent = null;
    }

    public static SculkDamageSyncPacket update(Entity entity, SculkDamageCapability data) {
        return new SculkDamageSyncPacket(entity, data);
    }

    public static SculkDamageSyncPacket warning(Entity entity, byte warning) {
        return new SculkDamageSyncPacket(entity, warning);
    }

    public static SculkDamageSyncPacket remove(Entity entity) {
        return new SculkDamageSyncPacket(entity);
    }

    public SculkDamageSyncPacket(FriendlyByteBuf buf) {
        this.entity = buf.readInt();
        this.type = buf.readEnum(Type.class);
        if (this.type == Type.DAMAGE) {
            this.damage = buf.readInt();
            this.protection = buf.readInt();
            this.maxProtection = buf.readInt();
        } else {
            this.damage = null;
            this.protection = null;
            this.maxProtection = null;
        }
        this.sculkWarningPercent = this.type != Type.REMOVE ? buf.readByte() : null;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entity);
        buf.writeEnum(type);
        if (type != Type.REMOVE) {
            if (type == Type.DAMAGE) {
                buf.writeInt(damage);
                buf.writeInt(protection);
                buf.writeInt(maxProtection);
            }
            buf.writeByte(sculkWarningPercent);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (type == Type.REMOVE) {
                SculkDamageCapability.ENTITIES.remove(entity);
            } else {
                SculkDamageCapability.ClientData data = SculkDamageCapability.ENTITIES.computeIfAbsent(entity, i -> new SculkDamageCapability.ClientData());
                if (type == Type.DAMAGE) {
                    data.set(damage, protection, maxProtection, sculkWarningPercent);
                } else {
                    data.setWarning(sculkWarningPercent);
                }
            }
        });
        return true;
    }

    private enum Type {
        DAMAGE,
        WARNING,
        REMOVE
    }
}