package dev.hipposgrumm.corrosive_sculk.network;

import dev.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import dev.hipposgrumm.corrosive_sculk.CorrosiveSculkClientside;
import dev.hipposgrumm.corrosive_sculk.config.Config;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
//? if forgebase {
import net.minecraftforge.fml.loading.FMLEnvironment;
//?} else {
/*import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
*///?}

public class SculkDamageSoundPacket implements CorrosiveSculkPacket {
    private final Type type;

    public SculkDamageSoundPacket(Type type) {
        this.type = type;
    }

    public SculkDamageSoundPacket(FriendlyByteBuf buf) {
        this.type = buf.readEnum(Type.class);
    }

    //? if fabric {
    /*public static final ResourceLocation ID = new ResourceLocation(CorrosiveSculk.MODID, "do_sound");

    @Override
    public ResourceLocation getID() {
        return ID;
    }
    *///?}

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(type);
    }

    public void handleClient() {
        //? if forge {
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