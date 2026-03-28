package dev.hipposgrumm.corrosive_sculk;

import dev.hipposgrumm.corrosive_sculk.block.MultifaceDoNothingBlock;
import dev.hipposgrumm.corrosive_sculk.capability.SculkDamageCapability;
import dev.hipposgrumm.corrosive_sculk.config.Config;
import dev.hipposgrumm.corrosive_sculk.enchantment.SculkToleranceEnchantment;
import dev.hipposgrumm.corrosive_sculk.network.SculkDamageSyncPacket;
import dev.hipposgrumm.corrosive_sculk.util.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
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
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
//? if forgebase {
import com.mojang.serialization.Codec;
import dev.hipposgrumm.corrosive_sculk.loot.LootModifierAddSculkToleranceItem;
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
//?} else {
/*import dev.hipposgrumm.corrosive_sculk.loot.LootModifiers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<MobEffect> POTION_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);
    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, MODID);
    private static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, MODID);
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);
    //?} else {
    /*private static final List<Supplier<?>> REGISTERED_ENTRIES = new ArrayList<>();
    *///?}

    public static final Supplier<Block> WOVEN_SCULK = createBlock("woven_sculk", () -> new Block(Block.Properties.copy(Blocks.SCULK).sound(SoundType.WOOL)));
    public static final Supplier<Block> WOVEN_SCULK_VEIN = createBlock("woven_sculk_vein", () -> new MultifaceDoNothingBlock(Block.Properties.copy(Blocks.SCULK_VEIN).sound(SoundType.WOOL)));
    public static final Supplier<BlockItem> WOVEN_SCULK_ITEM = createItem("woven_sculk", () -> new BlockItem(WOVEN_SCULK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> WOVEN_SCULK_VEIN_ITEM = createItem("woven_sculk_vein", () -> new BlockItem(WOVEN_SCULK_VEIN.get(), new Item.Properties()));

    public static final Supplier<MobEffect> SCULK_RESISTANCE = createMobEffect("sculk_resistance", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x063E44) {});
    public static final Supplier<Potion> SCULK_RESISTANCE_POTION = createPotion("sculk_resistance", () -> new Potion(new MobEffectInstance(SCULK_RESISTANCE.get(), 9600)));
    public static final Supplier<Potion> STRONG_SCULK_RESISTANCE_POTION = createPotion("strong_sculk_resistance", () -> new Potion(new MobEffectInstance(SCULK_RESISTANCE.get(), 9600, 1)));
    public static final Supplier<Potion> STRONGER_SCULK_RESISTANCE_POTION = createPotion("stronger_sculk_resistance", () -> new Potion(new MobEffectInstance(SCULK_RESISTANCE.get(), 9600, 2)));
    public static final Supplier<Enchantment> ENCHANTMENT_SCULK_TOLERANCE = createEnchantment("sculk_tolerance", () -> new SculkToleranceEnchantment(Enchantment.Rarity.UNCOMMON, EnchantmentCategory.ARMOR));

    public static final Supplier<SoundEvent> HEART_SCULK = createSoundEvent("player.heart_sculk", id -> SoundEvent.createFixedRangeEvent(id, 2.0F));
    public static final Supplier<SoundEvent> HEART_SCULK_RETURN = createSoundEvent("player.heart_sculk_return", id -> SoundEvent.createFixedRangeEvent(id, 2.0F));
    public static final Supplier<SoundEvent> HEART_SCULK_WARN = createSoundEvent("player.heart_sculk_warn", id -> SoundEvent.createFixedRangeEvent(id, 2.0F));

    //? if forgebase {
    public static final RegistryObject<Codec<LootModifierAddSculkToleranceItem>> MODIFIER_ADD_SCULK_TOLERANCE_ITEM = LOOT_MODIFIERS.register("sculk_tolerance_enchantment", LootModifierAddSculkToleranceItem.CODEC);
    //?}

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
    public CorrosiveSculk(FMLJavaModLoadingContext context) {
    //?} else {
    /*@Override
    public void onInitialize() {
    *///?}
        Config.registerConfig();

        //? if forgebase {
        IEventBus bus = context.getModEventBus();

        BLOCKS.register(bus);
        ITEMS.register(bus);
        POTION_EFFECTS.register(bus);
        POTIONS.register(bus);
        ENCHANTMENTS.register(bus);
        SOUND_EVENTS.register(bus);
        LOOT_MODIFIERS.register(bus);

        if (FMLEnvironment.dist.isClient()) {
            CorrosiveSculkClientside.setup(context, bus);
        }
        bus.addListener(CorrosiveSculk::commonSetup);
        bus.addListener(CorrosiveSculk::addCreativeItems);
        MinecraftForge.EVENT_BUS.addListener(CorrosiveSculk::registerReloadListeners);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, CorrosiveSculk::onAttachCapabilities);
        bus.addListener(CorrosiveSculk::registerCapabilities);
        MinecraftForge.EVENT_BUS.addListener(CorrosiveSculk::playerJoin);
        MinecraftForge.EVENT_BUS.addListener(CorrosiveSculk::onPlayerRespawn);
        MinecraftForge.EVENT_BUS.addListener(CorrosiveSculk::tickAllEntities);
        MinecraftForge.EVENT_BUS.addListener(CorrosiveSculk::onEntityDamaged);
        MinecraftForge.EVENT_BUS.addListener(CorrosiveSculk::onTotemUsed);
        MinecraftForge.EVENT_BUS.addListener(CorrosiveSculk::startTrackingEntity);
        MinecraftForge.EVENT_BUS.addListener(CorrosiveSculk::stopTrackingEntity);
        //?} else {
        /*for (Supplier<?> entry:REGISTERED_ENTRIES) {
            entry.get();
        }

        //? if fabric {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (source.isBuiltin())
                LootModifiers.register(id,tableBuilder);
        });
        //?}

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
                new ResourceLocation
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
                new ResourceLocation
                        (MODID, id), item.get());
        Supplier<T> is = () -> i;
        REGISTERED_ENTRIES.add(is);
        return is;
        *///?}
    }

    private static <T extends MobEffect> Supplier<T> createMobEffect(String id, Supplier<T> effect) {
        //? if forgebase {
        return POTION_EFFECTS.register(id,effect);
        //?} else {
        /*T e = Registry.register(BuiltInRegistries.MOB_EFFECT,
                //$ resourcelocation
                new ResourceLocation
                        (MODID, id), effect.get());
        Supplier<T> es = () -> e;
        REGISTERED_ENTRIES.add(es);
        return es;
        *///?}
    }

    private static <T extends Potion> Supplier<T> createPotion(String id, Supplier<T> potion) {
        //? if forgebase {
        return POTIONS.register(id,potion);
        //?} else {
        /*T p = Registry.register(BuiltInRegistries.POTION,
                //$ resourcelocation
                new ResourceLocation
                        (MODID, id), potion.get());
        Supplier<T> ps = () -> p;
        REGISTERED_ENTRIES.add(ps);
        return ps;
        *///?}
    }

    private static <T extends Enchantment> Supplier<T> createEnchantment(String id, Supplier<T> enchantment) {
        //? if forgebase {
        return ENCHANTMENTS.register(id,enchantment);
        //?} else {
        /*T e = Registry.register(BuiltInRegistries.ENCHANTMENT,
                //$ resourcelocation
                new ResourceLocation
                        (MODID, id), enchantment.get());
        Supplier<T> es = () -> e;
        REGISTERED_ENTRIES.add(es);
        return es;
        *///?}
    }

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

    private static void commonSetup(/*? if forgebase {*/FMLCommonSetupEvent event/*?}*/) {
        //? if forgebase
        event.enqueueWork(() -> {
            BrewHelper.createRecipes(Potions.AWKWARD, new ItemStack(Items.SCULK_CATALYST), SCULK_RESISTANCE_POTION.get());
            if (!Config.sculkResistInvul) {
                BrewHelper.createRecipes(SCULK_RESISTANCE_POTION.get(), new ItemStack(Items.GLOWSTONE_DUST), STRONG_SCULK_RESISTANCE_POTION.get());
                BrewHelper.createRecipes(STRONG_SCULK_RESISTANCE_POTION.get(), new ItemStack(Items.GLOWSTONE_DUST), STRONGER_SCULK_RESISTANCE_POTION.get());
            }
        //? if forgebase
        });

        //? if forgebase
        NetworkHelper.init();
    }

    private static void addCreativeItems(/*? if forgebase {*/BuildCreativeModeTabContentsEvent event/*?}*/) {
        //? if forgebase {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(WOVEN_SCULK_ITEM);
            event.accept(WOVEN_SCULK_VEIN_ITEM);
        }
        //?} else {
        /*ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS)
                .register((tab) -> {
                    tab.accept(WOVEN_SCULK_ITEM.get());
                    tab.accept(WOVEN_SCULK_VEIN_ITEM.get());
                });
        *///?}
    }

    private static void registerReloadListeners(/*? if forgebase {*/AddReloadListenerEvent event/*?}*/) {
        SCULK_DAMAGING_ENTITIES = new SculkDamagingEntitiesManager();
        //? if forgebase {
        event.addListener(SCULK_DAMAGING_ENTITIES);
        //?} else {
        /*ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(SCULK_DAMAGING_ENTITIES);
        *///?}
    }

    //? if forgebase {
    private static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity entity && !entity.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE).isPresent()) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath(MODID, "properties"), new SculkDamageCapability.Provider());
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
        //? if forgebase {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        player.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE)
        //?} else {
        /*ServerPlayer player = handler.getPlayer();
        ((PersistentDataAccessor) player).corrosive_sculk$getSculkData()
        *///?}
        .ifPresent(sculkDamage ->
                NetworkHelper.send(player, SculkDamageSyncPacket.update(player, sculkDamage))
        );
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
        player.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE)
        //?} else {
        /*if (alive) return;
        ((PersistentDataAccessor) player).corrosive_sculk$getSculkData()
        *///?}
        .ifPresent(sculkDamage ->
                NetworkHelper.send(player, SculkDamageSyncPacket.remove(old))
        );
    }

    private static void startTrackingEntity(
        //? if fabric {
            /*Entity target, ServerPlayer player) {
        *///?} else {
            PlayerEvent.StartTracking event) {
        Entity target = event.getTarget();
        //?}
        //? if forgebase {
        if (event.getEntity() instanceof ServerPlayer player)
            event.getTarget().getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE)
        //?} else {
        /*if (target instanceof LivingEntity)
            ((PersistentDataAccessor) target).corrosive_sculk$getSculkData()
        *///?}
            .ifPresent(sculkDamage ->
                NetworkHelper.send(player, SculkDamageSyncPacket.update(target, sculkDamage))
            );
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
            //? if forge {
            TickEvent.LevelTickEvent event
            //?} else {
            /*ServerLevel level
            *///?}
    ) {
        //? if forgebase {
        if (event.phase != TickEvent.Phase.START) return;
        if (event.side != LogicalSide.SERVER) return;
        if (!(event.level instanceof ServerLevel level)) return;
        //?} else {
        /*if (level.isClientSide) return;
        *///?}
        for (Entity e:level.getAllEntities()) {
            if (!(e instanceof LivingEntity entity)) continue;
            //? if forgebase {
            entity.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE)
            //?} else {
            /*((PersistentDataAccessor) entity).corrosive_sculk$getSculkData()
            *///?}
            .ifPresent(sculkDamage -> {
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
            });
        }
    }

    /*? if forgebase {*/private/*?} else {*//*public*//*?}*/ static void onEntityDamaged(
        //? if fabric {
            /*float damage, DamageSource source, LivingEntity entity) {
        *///?} else {
            LivingHurtEvent event) {
        float damage = event.getAmount();
        DamageSource source = event.getSource();
        LivingEntity entity = event.getEntity();
        //?}
        if (!Config.sculkHealCircumstance.hasSculkDamage) return;
        Entity attacker = source.getDirectEntity();
        if (attacker!=null && SculkDamaging.canSculkDamage(entity)) {
            int sculk = SCULK_DAMAGING_ENTITIES.getEntitySculkDamage(attacker);
            if (sculk>0) {
                //? if forgebase {
                entity.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE)
                //?} else {
                /*((PersistentDataAccessor) entity).corrosive_sculk$getSculkData()
                *///?}
                        .ifPresent(sculkDamage -> {
                            SculkDamaging.doSculkDamage((int)Math.min(damage,sculk)/2, sculkDamage, entity, attacker);
                            SculkDamaging.sendUpdate(sculkDamage, entity);
                        });
            }
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
        //? if forgebase {
        entity.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE)
         //?} else {
        /*((PersistentDataAccessor) entity).corrosive_sculk$getSculkData()
        *///?}
        .ifPresent(sculkDamage -> {
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
        });
    }
}