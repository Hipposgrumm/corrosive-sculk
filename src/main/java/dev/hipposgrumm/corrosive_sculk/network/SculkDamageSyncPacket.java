package dev.hipposgrumm.corrosive_sculk.network;

import dev.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import dev.hipposgrumm.corrosive_sculk.capability.SculkDamageCapability;
import dev.hipposgrumm.corrosive_sculk.config.Config;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

//? if >=1.20.5 {
/*import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Optional;
*///?}

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
        if (Config.sculkHealCircumstance.hasSculkDamage) {
            this.damage = data.getDamage();
            this.sculkWarningPercent = data.getWarning();
        } else {
            this.damage = 0;
            this.sculkWarningPercent = 0;
        }
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
    }

    private SculkDamageSyncPacket(Entity entity, byte warning) {
        this.entity = entity.getId();
        this.type = Type.WARNING;
        this.damage = null;
        this.protection = null;
        this.maxProtection = null;
        if (Config.sculkHealCircumstance.hasSculkDamage) {
            this.sculkWarningPercent = warning;
        } else {
            this.sculkWarningPercent = 0;
        }
    }

    private SculkDamageSyncPacket(Entity entity) {
        this.entity = entity.getId();
        this.type = Type.REMOVE;
        this.damage = null;
        this.protection = null;
        this.maxProtection = null;
        this.sculkWarningPercent = null;
    }

    public static final ResourceLocation ID =
            //$ resourcelocation
            ResourceLocation.fromNamespaceAndPath
                    (CorrosiveSculk.MODID, "sync_damage");


    @Override
    public ResourceLocation getID() {
        return ID;
    }

    //? if >=1.20.5 {
    /*public static final CustomPacketPayload.Type<SculkDamageSyncPacket> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SculkDamageSyncPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, p -> p.entity,
            ByteBufCodecs.INT, p -> p.type.ordinal(),
            ByteBufCodecs.optional(ByteBufCodecs.INT), p -> Optional.ofNullable(p.damage),
            ByteBufCodecs.optional(ByteBufCodecs.INT), p -> Optional.ofNullable(p.protection),
            ByteBufCodecs.optional(ByteBufCodecs.INT), p -> Optional.ofNullable(p.maxProtection),
            ByteBufCodecs.optional(ByteBufCodecs.BYTE), p -> Optional.ofNullable(p.sculkWarningPercent),
            SculkDamageSyncPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private SculkDamageSyncPacket(Integer entity, Integer typeOrdinal, Optional<Integer> damage, Optional<Integer> protection, Optional<Integer> maxProtection, Optional<Byte> sculkWarningPercent) {
        this.entity = entity;
        this.type = Type.values()[typeOrdinal];
        this.damage = damage.orElse(null);
        this.protection = protection.orElse(null);
        this.maxProtection = maxProtection.orElse(null);
        this.sculkWarningPercent = sculkWarningPercent.orElse(null);
    }
    *///?}

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

    @Override
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

    public void handleClient() {
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
    }

    private enum Type {
        DAMAGE,
        WARNING,
        REMOVE
    }
}