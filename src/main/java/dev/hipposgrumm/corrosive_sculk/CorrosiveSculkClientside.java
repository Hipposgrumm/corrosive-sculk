package dev.hipposgrumm.corrosive_sculk;

import dev.hipposgrumm.corrosive_sculk.config.ConfigScreen;
import dev.hipposgrumm.corrosive_sculk.network.SculkDamageSoundPacket;
import dev.hipposgrumm.corrosive_sculk.network.SculkDamageSyncPacket;
import dev.hipposgrumm.corrosive_sculk.util.HelperMethodsForMixins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

//? if neoforge {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
*///?} elif forge {
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
//?} else {
/*import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
    //? if >=1.20.5 {
    import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
    //?}
*///?}

import java.util.function.Consumer;

//? if forgebase {
@OnlyIn(Dist.CLIENT)
//?} else {
/*@Environment(EnvType.CLIENT)
*///?}
public class CorrosiveSculkClientside
        //? if fabric
        /*implements ClientModInitializer*/
{
    //? if forgebase {
    static void setup(/*? if neoforge {*//*ModContainer*//*?} else {*/FMLJavaModLoadingContext/*?}*/ context, IEventBus bus) {
    //?} else {
    /*@Override
    public void onInitializeClient() {
    *///?}
        HelperMethodsForMixins.loadStaticData();

        //? if forgebase {
        bus.addListener((Consumer<FMLClientSetupEvent>)
                (event -> clientSetup(event, context))
        );
        //?} else {
            /*//? if >=1.21.6 {
            /^BlockRenderLayerMap.putBlock(CorrosiveSculk.WOVEN_SCULK_VEIN.get(), RenderType.CUTOUT);
            ^///?} else {
            BlockRenderLayerMap.INSTANCE.putBlock(CorrosiveSculk.WOVEN_SCULK_VEIN.get(), RenderType.cutout());
            //?}
            //? if >=1.20.5 {
            PayloadTypeRegistry.playS2C().register(SculkDamageSyncPacket.TYPE, SculkDamageSyncPacket.CODEC);
            PayloadTypeRegistry.playS2C().register(SculkDamageSoundPacket.TYPE, SculkDamageSoundPacket.CODEC);
            //?}
        ClientPlayNetworking.registerGlobalReceiver(SculkDamageSyncPacket./^? if >=1.20.5 {^/TYPE/^?} else {^//^ID^//^?}^/,
            //? if >=1.20.5 {
            (packet, ctx) -> {
            Minecraft client = ctx.client();
            //?} else {
            /^(client, handler, buf, responseSender) -> {
            SculkDamageSyncPacket packet = new SculkDamageSyncPacket(buf);
            ^///?}
            client.execute(packet::handleClient);
        });
        ClientPlayNetworking.registerGlobalReceiver(SculkDamageSoundPacket./^? if >=1.20.5 {^/TYPE/^?} else {^//^ID^//^?}^/,
            //? if >=1.20.5 {
            (packet, ctx) -> {
            Minecraft client = ctx.client();
            //?} else {
            /^(client, handler, buf, responseSender) -> {
            SculkDamageSoundPacket packet = new SculkDamageSoundPacket(buf);
            ^///?}
            client.execute(packet::handleClient);
        });
        *///?}
    }

    //? if forgebase {
    private static void clientSetup(FMLClientSetupEvent event, /*? if neoforge {*//*ModContainer*//*?} else {*/FMLJavaModLoadingContext/*?}*/ context) {
        if (ModList.get().isLoaded("cloth_config")) event.enqueueWork(() -> {
            context.registerExtensionPoint(
                    //? if neoforge {
                    /*IConfigScreenFactory.class
                    *///?} else {
                    ConfigScreenHandler.ConfigScreenFactory.class
                    //?}
                    , /*? if forge {*/() -> new ConfigScreenHandler.ConfigScreenFactory/*?}*/
                            ((mc, parent) -> ConfigScreen.create(parent))
            );
        });
    }
    //?}

    public static void playHeartSound(SoundEvent sound, float volume) {
        Minecraft.getInstance().getSoundManager().play(
                new SimpleSoundInstance(sound./*? if >=1.21.2 {*//*location*//*?} else {*/getLocation/*?}*/(), SoundSource.PLAYERS, volume, 1f, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, true)
        );
    }
}
