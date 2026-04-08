package dev.hipposgrumm.corrosive_sculk.util;

import dev.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import dev.hipposgrumm.corrosive_sculk.network.CorrosiveSculkPacket;
import dev.hipposgrumm.corrosive_sculk.network.SculkDamageSoundPacket;
import dev.hipposgrumm.corrosive_sculk.network.SculkDamageSyncPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

//? if neoforge {
/*import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
*///?} elif forge {
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
//?} else {
/*import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
    //? if >=1.20.5 {
    import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
    //?}
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
*///?}

public class NetworkHelper {
    //? if forgebase && <1.20.5 {
    private static SimpleChannel NETWORK_INSTANCE;
    private static int packetID = 0;
    //?}

    //? if forgebase {
    public static void init(/*? if neoforge {*//*RegisterPayloadHandlersEvent event*//*?}*/) {
        //? if >=1.20.5 {
        /*event.registrar(CorrosiveSculk.MODID).versioned("2.0")
                .playToClient(
                        SculkDamageSyncPacket.TYPE,
                        SculkDamageSyncPacket.CODEC,
                        (packet, context) -> {
                            context.enqueueWork(packet::handleClient);
                        }
                ).playToClient(
                        SculkDamageSoundPacket.TYPE,
                        SculkDamageSoundPacket.CODEC,
                        (packet, context) -> {
                            context.enqueueWork(packet::handleClient);
                        }
                );
        *///?} else {
        NETWORK_INSTANCE = NetworkRegistry.ChannelBuilder
                .named(
                        //$ resourcelocation
                        ResourceLocation.fromNamespaceAndPath
                                (CorrosiveSculk.MODID, "messages"))
                .networkProtocolVersion(() -> "2.0")
                .clientAcceptedVersions("2.0"::equals)
                .serverAcceptedVersions("2.0"::equals)
                .simpleChannel();

        NETWORK_INSTANCE.messageBuilder(SculkDamageSyncPacket.class, packetID++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SculkDamageSyncPacket::new)
                .encoder(SculkDamageSyncPacket::toBytes)
                .consumerMainThread((packet, supplier) -> {
                    NetworkEvent.Context context = supplier.get();
                    context.enqueueWork(packet::handleClient);
                }).add();

        NETWORK_INSTANCE.messageBuilder(SculkDamageSoundPacket.class, packetID++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SculkDamageSoundPacket::new)
                .encoder(SculkDamageSoundPacket::toBytes)
                .consumerMainThread((packet, supplier) -> {
                    NetworkEvent.Context context = supplier.get();
                    context.enqueueWork(packet::handleClient);
                }).add();
        //?}
    }
    //?} elif >=1.20.5 {
    /*public static void init() {
        PayloadTypeRegistry.playS2C().register(SculkDamageSyncPacket.TYPE, SculkDamageSyncPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SculkDamageSoundPacket.TYPE, SculkDamageSoundPacket.CODEC);
    }
    *///?}

    @SuppressWarnings("SuspiciousIndentAfterControlStatement")
    public static void send(ServerPlayer player, CorrosiveSculkPacket packet) {
        if (player == null) return;
        //? if forgebase {
            //? if >=1.20.5 {
            /*PacketDistributor.sendToPlayer(player, packet);
            *///?} else {
            NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
            //?}
        //?} elif >=1.20.5 {
        /*ServerPlayNetworking.send(player, packet);
        *///?} else {
        /*FriendlyByteBuf data = PacketByteBufs.create();
        packet.toBytes(data);
        ServerPlayNetworking.send(player, packet.getID(), data);
        *///?}
    }

    @SuppressWarnings("SuspiciousIndentAfterControlStatement")
    public static void sendTracking(Entity entity, CorrosiveSculkPacket packet) {
        //? if forgebase {
        if (entity == null) return;
            //? if >=1.20.5 {
            /*PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet);
            *///?} else {
            NETWORK_INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), packet);
            //?}
        //?} elif >=1.20.5 {
        /*ServerPlayer player = entity instanceof ServerPlayer p ? p : null;
        for (ServerPlayer rec:PlayerLookup.tracking(entity)) {
            if (player == rec) player = null;
            ServerPlayNetworking.send(rec, packet);
        }
        if (player != null) ServerPlayNetworking.send(player, packet);
        *///?} else {
        /*FriendlyByteBuf data = PacketByteBufs.create();
        packet.toBytes(data);
        ServerPlayer player = entity instanceof ServerPlayer p ? p : null;
        for (ServerPlayer rec:PlayerLookup.tracking(entity)) {
            if (player == rec) player = null;
            ServerPlayNetworking.send(rec, packet.getID(), data);
        }
        if (player != null) ServerPlayNetworking.send(player, packet.getID(), data);
        *///?}
    }
}
