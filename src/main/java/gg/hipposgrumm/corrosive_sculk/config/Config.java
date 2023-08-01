package gg.hipposgrumm.corrosive_sculk.config;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import gg.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = CorrosiveSculk.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SCULK_BLOCKS = BUILDER
            .comment(" Blocks listed here will be counted as sculk block and will slowly wear down the player's health.\n It probably wouldn't be a good idea to add redstone blocks like the sculk sensor here.")
            .defineListAllowEmpty("Sculk Blocks", List.of("minecraft:sculk","minecraft:sculk_vein","minecraft:sculk_shrieker"), Config::validateBlocks);

    //private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SCULK_ENTITIES = BUILDER
    //        .comment(" Entities listed here will do sculk damage when attacking the player.\nThe maximum amount of damage an enemy can do is listed after.\nNote: Damage is calculated by heart and not by health point!")
    //        .defineListAllowEmpty("Sculk Entities", List.of("minecraft:warden,5"), Config::validateStrings);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static Set<Block> sculk_blocks;
    public static List<String> sculk_entities = new ArrayList<>();

    private static boolean validateBlocks(Object obj) {
        return obj instanceof String itemName && ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(itemName));
    }

    private static boolean validateStrings(Object obj) {
        return obj instanceof String;
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        sculk_blocks = SCULK_BLOCKS.get().stream()
                .map(itemName -> ForgeRegistries.BLOCKS.getValue(new ResourceLocation(itemName)))
                .collect(Collectors.toSet());

        //sculk_entities = SCULK_ENTITIES.get().stream().map(String::toString).toList();
    }
}
