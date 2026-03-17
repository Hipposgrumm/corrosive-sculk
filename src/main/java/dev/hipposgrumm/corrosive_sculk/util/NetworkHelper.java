package dev.hipposgrumm.corrosive_sculk.util;

import dev.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import dev.hipposgrumm.corrosive_sculk.network.CorrosiveSculkPacket;
import dev.hipposgrumm.corrosive_sculk.network.SculkDamageSoundPacket;
import dev.hipposgrumm.corrosive_sculk.network.SculkDamageSyncPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHelper {
    private static SimpleChannel NETWORK_INSTANCE;
    private static int packetID = 0;

    public static void init() {
        NETWORK_INSTANCE = NetworkRegistry.ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath(CorrosiveSculk.MODID, "messages"))
                .networkProtocolVersion(() -> "2.0")
                .clientAcceptedVersions("2.0"::equals)
                .serverAcceptedVersions("2.0"::equals)
                .simpleChannel();

        NETWORK_INSTANCE.messageBuilder(SculkDamageSyncPacket.class, packetID++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SculkDamageSyncPacket::new)
                .encoder(SculkDamageSyncPacket::toBytes)
                .consumerMainThread(SculkDamageSyncPacket::handle)
                .add();

        NETWORK_INSTANCE.messageBuilder(SculkDamageSoundPacket.class, packetID++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SculkDamageSoundPacket::new)
                .encoder(SculkDamageSoundPacket::toBytes)
                .consumerMainThread(SculkDamageSoundPacket::handle)
                .add();
    }

    public static void send(ServerPlayer player, CorrosiveSculkPacket packet) {
        if (player == null) return;
        NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendTracking(Entity entity, CorrosiveSculkPacket packet) {
        if (entity == null) return;
        NETWORK_INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), packet);
    }
}
