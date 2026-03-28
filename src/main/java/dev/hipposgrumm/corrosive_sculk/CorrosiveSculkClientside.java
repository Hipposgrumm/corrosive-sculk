package dev.hipposgrumm.corrosive_sculk;

import dev.hipposgrumm.corrosive_sculk.config.ConfigScreen;
import dev.hipposgrumm.corrosive_sculk.network.SculkDamageSoundPacket;
import dev.hipposgrumm.corrosive_sculk.network.SculkDamageSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

//? if forge {
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
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
    static void setup(FMLJavaModLoadingContext context, IEventBus bus) {
    //?} else {
    /*@Override
    public void onInitializeClient() {
    *///?}

        //? if forgebase {
        bus.addListener((Consumer<FMLClientSetupEvent>)
                (event -> clientSetup(event, context))
        );
        //?} else {
        /*ClientPlayNetworking.registerGlobalReceiver(SculkDamageSyncPacket.ID, (client, handler, buf, responseSender) -> {
            SculkDamageSyncPacket packet = new SculkDamageSyncPacket(buf);
            client.execute(packet::handleClient);
        });
        ClientPlayNetworking.registerGlobalReceiver(SculkDamageSoundPacket.ID, (client, handler, buf, responseSender) -> {
            SculkDamageSoundPacket packet = new SculkDamageSoundPacket(buf);
            client.execute(packet::handleClient);
        });
        *///?}
    }

    //? if forgebase {
    private static void clientSetup(FMLClientSetupEvent event, FMLJavaModLoadingContext context) {
        if (ModList.get().isLoaded("cloth_config")) event.enqueueWork(() -> {
            context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> ConfigScreen.create(parent)));
        });
    }
    //?}

    public static void playHeartSound(SoundEvent sound, float volume) {
        Minecraft.getInstance().getSoundManager().play(
                new SimpleSoundInstance(sound.getLocation(), SoundSource.PLAYERS, volume, 1f, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, true)
        );
    }
}
