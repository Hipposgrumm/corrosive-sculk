package dev.hipposgrumm.corrosive_sculk;

import dev.hipposgrumm.corrosive_sculk.block.MultifaceDoNothingBlock;
import dev.hipposgrumm.corrosive_sculk.capability.SculkDamageCapability;
import dev.hipposgrumm.corrosive_sculk.config.Config;
//? if <1.21
import dev.hipposgrumm.corrosive_sculk.enchantment.SculkToleranceEnchantment;
import dev.hipposgrumm.corrosive_sculk.network.SculkDamageSyncPacket;
import dev.hipposgrumm.corrosive_sculk.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.packs.PackType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
//? if forgebase {
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.hipposgrumm.corrosive_sculk.loot.LootModifierAddSculkToleranceItem;
    //? if neoforge {
    /*import net.neoforged.bus.api.IEventBus;
    import net.neoforged.fml.ModContainer;
    import net.neoforged.fml.common.Mod;
    import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
    import net.neoforged.fml.loading.FMLEnvironment;
    import net.neoforged.neoforge.attachment.AttachmentType;
    import net.neoforged.neoforge.common.NeoForge;
    import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
    import net.neoforged.neoforge.event.AddReloadListenerEvent;
    import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
    import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
    //? if >=1.21 {
    import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
    //?} else {
    /^import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;
    ^///?}
    import net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent;
    import net.neoforged.neoforge.event.entity.player.PlayerEvent;
    import net.neoforged.neoforge.event.tick.LevelTickEvent;
    import net.neoforged.neoforge.registries.DeferredRegister;
    import net.neoforged.neoforge.registries.NeoForgeRegistries;
    *///?} else {
    import net.minecraftforge.common.MinecraftForge;
    import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
    import net.minecraftforge.common.loot.IGlobalLootModifier;
    import net.minecraftforge.event.AddReloadListenerEvent;
    import net.minecraftforge.event.AttachCapabilitiesEvent;
    import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
    import net.minecraftforge.event.TickEvent;
    import net.minecraftforge.event.entity.living.LivingHurtEvent;
    import net.minecraftforge.event.entity.living.LivingUseTotemEvent;
    import net.minecraftforge.event.entity.player.PlayerEvent;
    import net.minecraftforge.eventbus.api.IEventBus;
    import net.minecraftforge.fml.LogicalSide;
    import net.minecraftforge.fml.common.Mod;
    import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
    import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
    import net.minecraftforge.fml.loading.FMLEnvironment;
    import net.minecraftforge.registries.DeferredRegister;
    import net.minecraftforge.registries.ForgeRegistries;
    import net.minecraftforge.registries.RegistryObject;
    //?}
//?} else {
/*import dev.hipposgrumm.corrosive_sculk.loot.LootModifiers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
*///?}
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

//? if forgebase
@Mod(CorrosiveSculk.MODID)
public class CorrosiveSculk
    //? if fabric
    /*implements ModInitializer*/
{
    public static final String MODID = "corrosive_sculk";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    //? if forgebase {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(/*? if neoforge {*//*BuiltInRegistries.BLOCK*//*?} else {*/ForgeRegistries.BLOCKS/*?}*/, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(/*? if neoforge {*//*BuiltInRegistries.ITEM*//*?} else {*/ForgeRegistries.ITEMS/*?}*/, MODID);
    private static final DeferredRegister<MobEffect> POTION_EFFECTS = DeferredRegister.create(/*? if neoforge {*//*BuiltInRegistries.MOB_EFFECT*//*?} else {*/ForgeRegistries.MOB_EFFECTS/*?}*/, MODID);
    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(/*? if neoforge {*//*BuiltInRegistries.POTION*//*?} else {*/ForgeRegistries.POTIONS/*?}*/, MODID);
    //? if <1.21
    private static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(/*? if neoforge {*//*BuiltInRegistries.ENCHANTMENT*//*?} else {*/ForgeRegistries.ENCHANTMENTS/*?}*/, MODID);
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(/*? if neoforge {*//*BuiltInRegistries.SOUND_EVENT*//*?} else {*/ForgeRegistries.SOUND_EVENTS/*?}*/, MODID);
    private static final DeferredRegister</*? if neoforge {*//*MapCodec*//*?} else {*/Codec/*?}*/<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(/*? if neoforge {*//*NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS*//*?} else {*/ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS/*?}*/, MODID);
    //? if neoforge
    /*private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);*/
    //?} else {
    /*private static final List<Supplier<?>> REGISTERED_ENTRIES = new ArrayList<>();
    *///?}

    public static final Supplier<Block> WOVEN_SCULK = createBlock("woven_sculk", () -> new Block(Block.Properties./*? if >=1.20.3 {*//*ofFullCopy*//*?} else {*/copy/*?}*/(Blocks.SCULK).sound(SoundType.WOOL)));
    public static final Supplier<Block> WOVEN_SCULK_VEIN = createBlock("woven_sculk_vein", () -> new MultifaceDoNothingBlock(Block.Properties./*? if >=1.20.3 {*//*ofFullCopy*//*?} else {*/copy/*?}*/(Blocks.SCULK_VEIN).sound(SoundType.WOOL)));
    public static final Supplier<BlockItem> WOVEN_SCULK_ITEM = createItem("woven_sculk", () -> new BlockItem(WOVEN_SCULK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> WOVEN_SCULK_VEIN_ITEM = createItem("woven_sculk_vein", () -> new BlockItem(WOVEN_SCULK_VEIN.get(), new Item.Properties()));

    public static final Supplier</*? if >1.20.1 {*//*Holder<MobEffect>*//*?} else {*/MobEffect/*?}*/> SCULK_RESISTANCE = createMobEffect("sculk_resistance", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x063E44) {});
    public static final Supplier</*? if >1.20.1 {*//*Holder<Potion>*//*?} else {*/Potion/*?}*/> SCULK_RESISTANCE_POTION = createPotion("sculk_resistance", () -> new Potion(new MobEffectInstance(SCULK_RESISTANCE.get(), 9600)));
    public static final Supplier</*? if >1.20.1 {*//*Holder<Potion>*//*?} else {*/Potion/*?}*/> STRONG_SCULK_RESISTANCE_POTION = createPotion("strong_sculk_resistance", () -> new Potion(new MobEffectInstance(SCULK_RESISTANCE.get(), 9600, 1)));
    public static final Supplier</*? if >1.20.1 {*//*Holder<Potion>*//*?} else {*/Potion/*?}*/> STRONGER_SCULK_RESISTANCE_POTION = createPotion("stronger_sculk_resistance", () -> new Potion(new MobEffectInstance(SCULK_RESISTANCE.get(), 9600, 2)));

    //? if >=1.21 {
    /*public static final ResourceKey<Enchantment> ENCHANTMENT_SCULK_TOLERANCE = ResourceKey.create(Registries.ENCHANTMENT,
            //$ resourcelocation
            ResourceLocation.fromNamespaceAndPath
                    (MODID, "sculk_tolerance")
    );
    *///?} else {
        //? if >=1.20.5 {
        /*public static final TagKey<Item> SCULK_TOLERANCE_ENCHANTABLE = TagKey.create(Registries.ITEM,
                //$ resourcelocation
                ResourceLocation.fromNamespaceAndPath
                        (MODID, "sculk_tolerance_enchantable"));
        *///?}
    public static final Supplier<SculkToleranceEnchantment> ENCHANTMENT_SCULK_TOLERANCE = createEnchantment("sculk_tolerance", SculkToleranceEnchantment::new);
    //?}

    public static final Supplier<SoundEvent> HEART_SCULK = createSoundEvent("player.heart_sculk", id -> SoundEvent.createFixedRangeEvent(id, 2.0F));
    public static final Supplier<SoundEvent> HEART_SCULK_RETURN = createSoundEvent("player.heart_sculk_return", id -> SoundEvent.createFixedRangeEvent(id, 2.0F));
    public static final Supplier<SoundEvent> HEART_SCULK_WARN = createSoundEvent("player.heart_sculk_warn", id -> SoundEvent.createFixedRangeEvent(id, 2.0F));

    //? if forgebase {
    public static final Supplier</*? if neoforge {*//*MapCodec*//*?} else {*/Codec/*?}*/<LootModifierAddSculkToleranceItem>> MODIFIER_ADD_SCULK_TOLERANCE_ITEM = LOOT_MODIFIERS.register("sculk_tolerance_enchantment", LootModifierAddSculkToleranceItem.CODEC);
    //?}
    //? if neoforge {
    /*public static final Supplier<AttachmentType<SculkDamageCapability>> SCULK_DAMAGE_ATTACHMENT = ATTACHMENT_TYPES.register("sculk_damage", () -> AttachmentType.serializable(SculkDamageCapability::new).build());
    *///?}

    public static final TagKey<Block> SCULK_BLOCKS = TagKey.create(Registries.BLOCK,
            //$ resourcelocation
            ResourceLocation.fromNamespaceAndPath
                    (MODID, "sculk_blocks"));
    public static final TagKey<EntityType<?>> SCULK_IMMUNE_ENTITIES = TagKey.create(Registries.ENTITY_TYPE,
            //$ resourcelocation
            ResourceLocation.fromNamespaceAndPath
                    (MODID, "sculk_immune"));

    public static final ResourceKey<DamageType> SCULK_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            //$ resourcelocation
            ResourceLocation.fromNamespaceAndPath
                    (MODID, "sculk_damage"));

    private static SculkDamagingEntitiesManager SCULK_DAMAGING_ENTITIES;

    //? if forgebase {
    public CorrosiveSculk(/*? if neoforge {*//*IEventBus modBus, ModContainer*//*?} else {*/FMLJavaModLoadingContext/*?}*/ context) {
    //?} else {
    /*@Override
    public void onInitialize() {
    *///?}
        Config.registerConfig();

        //? if neoforge {
        /*IEventBus forgeBus = NeoForge.EVENT_BUS;
        *///?} elif forge {
        IEventBus modBus = context.getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        //?}

        //? if forgebase {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        POTION_EFFECTS.register(modBus);
        POTIONS.register(modBus);
        //? if <1.21
        ENCHANTMENTS.register(modBus);
        SOUND_EVENTS.register(modBus);
        LOOT_MODIFIERS.register(modBus);
        //? if neoforge
        /*ATTACHMENT_TYPES.register(modBus);*/

        if (FMLEnvironment.dist.isClient()) {
            CorrosiveSculkClientside.setup(context, modBus);
        }
        //? if >=1.20.5 {
        /*forgeBus.addListener(CorrosiveSculk::brewingRegister);
        *///?} else {
        modBus.addListener(CorrosiveSculk::commonSetup);
        //?}
        modBus.addListener(CorrosiveSculk::addCreativeItems);
        forgeBus.addListener(CorrosiveSculk::registerReloadListeners);
            //? if neoforge {
            /*modBus.addListener(NetworkHelper::init);
            *///?} else {
            forgeBus.addGenericListener(Entity.class, CorrosiveSculk::onAttachCapabilities);
            modBus.addListener(CorrosiveSculk::registerCapabilities);
            //?}
        forgeBus.addListener(CorrosiveSculk::playerJoin);
        forgeBus.addListener(CorrosiveSculk::onPlayerRespawn);
        forgeBus.addListener(CorrosiveSculk::tickAllEntities);
        forgeBus.addListener(CorrosiveSculk::onEntityDamaged);
        forgeBus.addListener(CorrosiveSculk::onTotemUsed);
        forgeBus.addListener(CorrosiveSculk::startTrackingEntity);
        forgeBus.addListener(CorrosiveSculk::stopTrackingEntity);
        //?} else {
        /*for (Supplier<?> entry:REGISTERED_ENTRIES) {
            //? if >=1.20.5 {
            if (entry.get() instanceof Holder<?> h) h.value();
            //?} else {
            /^entry.get();
            ^///?}
        }

        LootTableEvents.MODIFY.register((/^? if <1.20.5 {^//^resourceManager, lootManager, id^//^?} else {^/key/^?}^/, tableBuilder, source) -> {
            //? if >=1.20.5
            ResourceLocation id = key.location();
            if (source.isBuiltin())
                LootModifiers.register(id,tableBuilder);
        });

        CorrosiveSculk.commonSetup();
        CorrosiveSculk.addCreativeItems();
        CorrosiveSculk.registerReloadListeners();
        ServerPlayConnectionEvents.JOIN.register(CorrosiveSculk::playerJoin);
        ServerPlayerEvents.COPY_FROM.register(CorrosiveSculk::onPlayerRespawn);
        ServerTickEvents.END_WORLD_TICK.register(CorrosiveSculk::tickAllEntities);
        EntityTrackingEvents.START_TRACKING.register(CorrosiveSculk::startTrackingEntity);
        EntityTrackingEvents.STOP_TRACKING.register(CorrosiveSculk::stopTrackingEntity);
        *///?}
    }

    private static <T extends Block> Supplier<T> createBlock(String id, Supplier<T> block) {
        //? if forgebase {
        return BLOCKS.register(id,block);
        //?} else {
        /*T b = Registry.register(BuiltInRegistries.BLOCK,
                //$ resourcelocation
                ResourceLocation.fromNamespaceAndPath
                        (MODID, id), block.get());
        Supplier<T> bs = () -> b;
        REGISTERED_ENTRIES.add(bs);
        return bs;
        *///?}
    }

    private static <T extends Item> Supplier<T> createItem(String id, Supplier<T> item) {
        //? if forgebase {
        return ITEMS.register(id,item);
        //?} else {
        /*T i = Registry.register(BuiltInRegistries.ITEM,
                //$ resourcelocation
                ResourceLocation.fromNamespaceAndPath
                        (MODID, id), item.get());
        Supplier<T> is = () -> i;
        REGISTERED_ENTRIES.add(is);
        return is;
        *///?}
    }

    @SuppressWarnings("unchecked")
    private static <T extends MobEffect> Supplier</*? if >1.20.1 {*//*Holder<T>*//*?} else {*/T/*?}*/> createMobEffect(String id, Supplier<T> effect) {
        //? if forgebase {
            //? if >1.20.1
            /*Holder<T> e = (Holder<T>) POTION_EFFECTS.register(id,effect);*/
        return /*? if >1.20.1 {*//*e::getDelegate*//*?} else {*/POTION_EFFECTS.register(id,effect)/*?}*/;
        //?} else {
        /*/^? if >1.20.1 {^/Holder<T>/^?} else {^//^T^//^?}^/ e = /^? if >1.20.1 {^/(Holder<T>)/^?}^/ Registry.
                //? if >1.20.1 {
                registerForHolder
                //?} else {
                /^register
                ^///?}
                        (BuiltInRegistries.MOB_EFFECT,
                //$ resourcelocation
                ResourceLocation.fromNamespaceAndPath
                        (MODID, id), effect.get());
        Supplier</^? if >1.20.1 {^/Holder<T>/^?} else {^//^T^//^?}^/> es = () -> e;
        REGISTERED_ENTRIES.add(es);
        return es;
        *///?}
    }

    @SuppressWarnings("unchecked")
    private static <T extends Potion> Supplier</*? if >1.20.1 {*//*Holder<T>*//*?} else {*/T/*?}*/> createPotion(String id, Supplier<T> potion) {
        //? if forgebase {
            //? if >1.20.1
            /*Holder<T> p = (Holder<T>) POTIONS.register(id,potion);*/
        return /*? if >1.20.1 {*//*p::getDelegate*//*?} else {*/POTIONS.register(id,potion)/*?}*/;
        //?} else {
        /*/^? if >1.20.1 {^/Holder<T>/^?} else {^//^T^//^?}^/ p = /^? if >1.20.1 {^/(Holder<T>)/^?}^/ Registry.
                //? if >1.20.1 {
                registerForHolder
                //?} else {
                /^register
                ^///?}
                        (BuiltInRegistries.POTION,
                //$ resourcelocation
                ResourceLocation.fromNamespaceAndPath
                        (MODID, id), potion.get());
        Supplier</^? if >1.20.1 {^/Holder<T>/^?} else {^//^T^//^?}^/> ps = () -> p;
        REGISTERED_ENTRIES.add(ps);
        return ps;
        *///?}
    }

    //? if <1.21 {
    private static <T extends Enchantment> Supplier<T> createEnchantment(String id, Supplier<T> enchantment) {
        //? if forgebase {
        return ENCHANTMENTS.register(id,enchantment);
        //?} else {
        /*T e = Registry.register(BuiltInRegistries.ENCHANTMENT,
                //$ resourcelocation
                ResourceLocation.fromNamespaceAndPath
                        (MODID, id), enchantment.get());
        Supplier<T> es = () -> e;
        REGISTERED_ENTRIES.add(es);
        return es;
        *///?}
    }
    //?}

    private static <T extends SoundEvent> Supplier<T> createSoundEvent(String id, Function<ResourceLocation, T> sound) {
        ResourceLocation location =
                //$ resourcelocation
                ResourceLocation.fromNamespaceAndPath
                        (MODID, id);
        //? if forgebase {
        return SOUND_EVENTS.register(id,() -> sound.apply(location));
        //?} else {
        /*T s = Registry.register(BuiltInRegistries.SOUND_EVENT, location, sound.apply(location));
        Supplier<T> ss = () -> s;
        REGISTERED_ENTRIES.add(ss);
        return ss;
        *///?}
    }

    //? if forgebase && >=1.20.5 {
    /*private static void brewingRegister(/^? if forgebase {^/RegisterBrewingRecipesEvent event/^?}^/) {
    *///?} else {
    private static void commonSetup(/*? if forgebase {*/FMLCommonSetupEvent event/*?}*/) {
    //?}
        //? if forgebase && <1.20.5
        event.enqueueWork(() -> {
            BrewHelper.createRecipes(/*? if forgebase && >=1.20.5 {*//*event,*//*?}*/ Potions.AWKWARD, Items.SCULK_CATALYST, SCULK_RESISTANCE_POTION.get());
            if (!Config.sculkResistInvul) {
                BrewHelper.createRecipes(/*? if forgebase && >=1.20.5 {*//*event,*//*?}*/ SCULK_RESISTANCE_POTION.get(), Items.GLOWSTONE_DUST, STRONG_SCULK_RESISTANCE_POTION.get());
                BrewHelper.createRecipes(/*? if forgebase && >=1.20.5 {*//*event,*//*?}*/ STRONG_SCULK_RESISTANCE_POTION.get(), Items.GLOWSTONE_DUST, STRONGER_SCULK_RESISTANCE_POTION.get());
            }
        //? if forgebase && <1.20.5
        });

        //? if forge
        NetworkHelper.init();
    }

    private static void addCreativeItems(/*? if forgebase {*/BuildCreativeModeTabContentsEvent event/*?}*/) {
        //? if forgebase {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(WOVEN_SCULK_ITEM.get());
            event.accept(WOVEN_SCULK_VEIN_ITEM.get());
        }
            //? if >=1.20.5 && <1.21 {
            /*else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
                int max = CorrosiveSculk.ENCHANTMENT_SCULK_TOLERANCE.get().getMaxLevel();
                for (int i=1;i<=max;i++)
                    event.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(CorrosiveSculk.ENCHANTMENT_SCULK_TOLERANCE.get(), i)), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                event.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(CorrosiveSculk.ENCHANTMENT_SCULK_TOLERANCE.get(), max)), CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
            }
            *///?}
        //?} else {
        /*ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS)
                .register((tab) -> {
                    tab.accept(WOVEN_SCULK_ITEM.get());
                    tab.accept(WOVEN_SCULK_VEIN_ITEM.get());
                });
            //? if >=1.20.5 && <1.21 {
            /^ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                    .register((tab) -> {
                        int max = CorrosiveSculk.ENCHANTMENT_SCULK_TOLERANCE.get().getMaxLevel();
                        for (int i=1;i<=max;i++)
                            tab.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(CorrosiveSculk.ENCHANTMENT_SCULK_TOLERANCE.get(), i)), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                        tab.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(CorrosiveSculk.ENCHANTMENT_SCULK_TOLERANCE.get(), max)), CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
                    });
            ^///?}
        *///?}
    }

    private static void registerReloadListeners(/*? if forgebase {*/AddReloadListenerEvent event/*?}*/) {
        SCULK_DAMAGING_ENTITIES = new SculkDamagingEntitiesManager();
        //? if forgebase {
        event.addListener(SCULK_DAMAGING_ENTITIES);
        //?} else {
        /*ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(SCULK_DAMAGING_ENTITIES);
        *///?}
    }

    //? if forge {
    private static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity entity && !entity.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE).isPresent()) {
            event.addCapability(
                    //$ resourcelocation
                    ResourceLocation.fromNamespaceAndPath
                            (MODID, "properties"), new SculkDamageCapability.Provider());
        }
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(SculkDamageCapability.class);
    }
    //?}

    private static void playerJoin(
            //? if forgebase {
            PlayerEvent.PlayerLoggedInEvent event
            //?} else {
            /*ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server
            *///?}
    ) {
        //? if forgebase
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        //? if fabric
        /*ServerPlayer player = handler.getPlayer();*/
        SculkDamageCapability sculkDamage =
                //? if neoforge {
                /*player.getData(SCULK_DAMAGE_ATTACHMENT);
                *///?} elif forge {
                player.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE).orElse(null);
                //?} else {
                /*((PersistentDataAccessor) player).corrosive_sculk$getSculkData();
                *///?}
        NetworkHelper.send(player, SculkDamageSyncPacket.update(player, sculkDamage));
    }

    public static void onPlayerRespawn(
            //? if forgebase {
            PlayerEvent.Clone event
            //?} else {
            /*ServerPlayer old, ServerPlayer player, boolean alive
            *///?}
    ) {
        //? if forgebase {
        if (!event.isWasDeath() || !(event.getEntity() instanceof ServerPlayer player)) return;
        Player old = event.getOriginal();
        //?} else {
        /*if (alive) return;
        *///?}
        NetworkHelper.send(player, SculkDamageSyncPacket.remove(old));
    }

    private static void startTrackingEntity(
        //? if fabric {
            /*Entity target, ServerPlayer player) {
        *///?} else {
            PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Entity target = event.getTarget();
        //?}
        if (!(target instanceof LivingEntity)) return;
        SculkDamageCapability sculkDamage =
                //? if neoforge {
                /*target.getData(SCULK_DAMAGE_ATTACHMENT);
                *///?} elif forge {
                target.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE).orElse(null);
                //?} else {
                /*((PersistentDataAccessor) target).corrosive_sculk$getSculkData();
                *///?}
        NetworkHelper.send(player, SculkDamageSyncPacket.update(target, sculkDamage));
    }

    private static void stopTrackingEntity(
        //? if fabric {
            /*Entity target, ServerPlayer player) {
        *///?} else {
            PlayerEvent.StopTracking event) {
        Entity target = event.getTarget();
        //?}
        //? if forgebase
        if (event.getEntity() instanceof ServerPlayer player)
            NetworkHelper.send(player, SculkDamageSyncPacket.remove(target));
    }

    private static void tickAllEntities(
            //? if neoforge {
            /*LevelTickEvent.Pre event
            *///?} elif forge {
            TickEvent.LevelTickEvent event
            //?} else {
            /*ServerLevel level
            *///?}
    ) {
        //? if forgebase {
            //? if forge {
            if (event.phase != TickEvent.Phase.START) return;
            if (event.side != LogicalSide.SERVER) return;
            if (!(event.level instanceof ServerLevel level)) return;
            //?} else {
            /*if (!(event.getLevel() instanceof ServerLevel level)) return;
            *///?}
        //?} else {
        /*if (level.isClientSide) return;
        *///?}
        for (Entity e:level.getAllEntities()) {
            if (!(e instanceof LivingEntity entity)) continue;
            SculkDamageCapability sculkDamage =
                    //? if neoforge {
                    /*entity.getData(SCULK_DAMAGE_ATTACHMENT);
                    *///?} elif forge {
                    entity.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE).orElse(null);
                    //?} else {
                    /*((PersistentDataAccessor) entity).corrosive_sculk$getSculkData();
                    *///?}
            boolean updateFull = false, updateWarn = false;
            boolean hasContact, cantBeSafe;
            if (SculkDamaging.canSculkDamage(entity)) {
                boolean isPlayer = entity instanceof ServerPlayer || entity.getControllingPassenger() instanceof ServerPlayer;
                // Damaging
                cantBeSafe = isPlayer && !entity.onGround() && sculkDamage.getDamageTimer() != null;
                hasContact = isPlayer ?
                        SculkDamaging.detectSculkCollisionPlayer(entity, level) : // For players do more complex stuff.
                        SculkDamaging.detectSculkCollision(entity, level);        // For other entities simpler checks will do.
                if (cantBeSafe || hasContact) {
                    boolean upd = SculkDamaging.handleSculkPresence(sculkDamage, entity, hasContact, cantBeSafe);
                    if (upd) updateFull = true;
                    else updateWarn = true;
                }
            } else {
                hasContact = false;
                cantBeSafe = false;
            }

            // Healing
            if (SculkDamaging.handleSafeAndShouldHeal(sculkDamage, entity, hasContact, cantBeSafe)) {
                boolean upd = SculkDamaging.handleHealing(sculkDamage, entity, level);
                if (upd) updateFull = true;
                else updateWarn = true;
            }
            if (SculkDamaging.handleSculkResistance(sculkDamage, entity))
                updateFull = true;
            if (updateFull) SculkDamaging.sendUpdate(sculkDamage, entity);
            else if (updateWarn) SculkDamaging.sendUpdateWarn(sculkDamage, entity);
        }
    }

    /*? if forgebase {*/private/*?} else {*//*public*//*?}*/ static void onEntityDamaged(
            //? if fabric {
            /*float damage, DamageSource source, LivingEntity entity) {
        *///?} else {
            /*? if >=1.21 {*//*LivingDamageEvent.Pre*//*?} else {*/LivingHurtEvent/*?}*/ event) {
        float damage = event./*? if >=1.21 {*//*getNewDamage*//*?} else {*/getAmount/*?}*/();
        DamageSource source = event.getSource();
        LivingEntity entity = event.getEntity();
        //?}
        if (!Config.sculkHealCircumstance.hasSculkDamage) return;
        Entity attacker = source.getDirectEntity();
        if (attacker!=null && SculkDamaging.canSculkDamage(entity)) {
            int sculk = Math.min((int) damage,2*SCULK_DAMAGING_ENTITIES.getEntitySculkDamage(attacker));
            if (sculk<=0) return;
            int div = 4;
            float prot = 0;
            //? if >=1.21
            /*Holder<Enchantment> sculkTolerance = entity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ENCHANTMENT_SCULK_TOLERANCE);*/
            for (ItemStack armor:entity./*? if >=1.20.5 {*//*getArmorAndBodyArmorSlots*//*?} else {*/getArmorSlots/*?}*/()) {
                int amount = 2*EnchantmentHelper.getItemEnchantmentLevel(
                        //? if >=1.21 {
                        /*sculkTolerance,
                        *///?} else {
                        CorrosiveSculk.ENCHANTMENT_SCULK_TOLERANCE.get(),
                        //?}
                        armor
                );
                if (armor.getItem() instanceof /*? if >=1.20.5 {*//*AnimalArmorItem*//*?} else {*/HorseArmorItem/*?}*/) {
                    prot = amount;
                    div = 1;
                    break;
                } else {
                    prot += amount;
                }
            }
            if (prot > 0) sculk -= Math.round(prot / div);
            if (sculk<=0) return;
            int actualSculkDamage = sculk;
            SculkDamageCapability sculkDamage =
                    //? if neoforge {
                    /*entity.getData(SCULK_DAMAGE_ATTACHMENT);
                    *///?} elif forge {
                    entity.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE).orElse(null);
                    //?} else {
                    /*((PersistentDataAccessor) entity).corrosive_sculk$getSculkData();
                    *///?}
            SculkDamaging.doSculkDamage(actualSculkDamage/2, sculkDamage, entity, attacker);
            SculkDamaging.sendUpdate(sculkDamage, entity);
        }
    }

    /*? if forgebase {*/private/*?} else {*//*public*//*?}*/ static void onTotemUsed(
        //? if fabric {
            /*LivingEntity entity) {
        *///?} else {
            LivingUseTotemEvent event) {
        LivingEntity entity = event.getEntity();
        //?}
        if (!Config.sculkHealCircumstance.hasSculkDamage) return;

        SculkDamageCapability sculkDamage =
                //? if neoforge {
                /*entity.getData(SCULK_DAMAGE_ATTACHMENT);
                *///?} elif forge {
                entity.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE).orElse(null);
                //?} else {
                /*((PersistentDataAccessor) entity).corrosive_sculk$getSculkData();
                *///?}
        float remaining = entity.getMaxHealth() - (sculkDamage.getDamage()*2);
        sculkDamage.decreaseDamage(1);
        sculkDamage.setForcedHealing(4-(int)remaining);
        sculkDamage.setHealTimer(10);
        if (sculkDamage.getDamageTimer() != null) {
            sculkDamage.setDamageTimer(80);
            sculkDamage.setWarning(0);
            entity.addEffect(new MobEffectInstance(SCULK_RESISTANCE.get(), 300));
        }
        if (entity instanceof ServerPlayer player) NetworkHelper.send(player, SculkDamageSyncPacket.warning(entity, sculkDamage.getWarning()));
    }
}