package dev.hipposgrumm.corrosive_sculk.network;

import dev.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import dev.hipposgrumm.corrosive_sculk.CorrosiveSculkClientside;
import dev.hipposgrumm.corrosive_sculk.config.Config;
import net.minecraft.network.FriendlyByteBuf;
//? if >=1.20.5 {
/*import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
*///?}
import net.minecraft.resources.ResourceLocation;
//? if neoforge {
/*import net.neoforged.fml.loading.FMLEnvironment;
*///?} elif forge {
import net.minecraftforge.fml.loading.FMLEnvironment;
//?} else {
/*import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
*///?}

//? if >=1.20.5
/*import net.minecraft.network.protocol.common.custom.CustomPacketPayload;*/

public class SculkDamageSoundPacket implements CorrosiveSculkPacket {
    private final Type type;

    public SculkDamageSoundPacket(Type type) {
        this.type = type;
    }

    public SculkDamageSoundPacket(FriendlyByteBuf buf) {
        this.type = buf.readEnum(Type.class);
    }

    public static final ResourceLocation ID =
            //$ resourcelocation
            ResourceLocation.fromNamespaceAndPath
                    (CorrosiveSculk.MODID, "do_sound");

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    //? if >=1.20.5 {
    /*public static final CustomPacketPayload.Type<SculkDamageSoundPacket> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SculkDamageSoundPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, p -> p.type.ordinal(),
            SculkDamageSoundPacket::new
    );
    @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    private SculkDamageSoundPacket(Integer typeOrdinal) {
        this.type = Type.values()[typeOrdinal];
    }
    *///?}

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(type);
    }

    public void handleClient() {
        //? if forgebase {
        if (!FMLEnvironment.dist.isClient()) return;
        //?} else {
        /*if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) return;
        *///?}
        CorrosiveSculkClientside.playHeartSound((switch (type) {
            case DAMAGE -> CorrosiveSculk.HEART_SCULK;
            case HEAL -> CorrosiveSculk.HEART_SCULK_RETURN;
            case WARN -> CorrosiveSculk.HEART_SCULK_WARN;
        }).get(), type == Type.WARN ? (Config.sculkWarnSound?0.4f:0f) : 1f);
    }

    public enum Type {
        DAMAGE,
        HEAL,
        WARN
    }
}