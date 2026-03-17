package dev.hipposgrumm.corrosive_sculk;

import com.mojang.serialization.Codec;
import dev.hipposgrumm.corrosive_sculk.block.MultifaceDoNothingBlock;
import dev.hipposgrumm.corrosive_sculk.enchantment.SculkToleranceEnchantment;
import dev.hipposgrumm.corrosive_sculk.loot.LootModifierAddSculkToleranceItem;
import dev.hipposgrumm.corrosive_sculk.network.SculkDamageSyncPacket;
import dev.hipposgrumm.corrosive_sculk.capability.SculkDamageCapability;
import dev.hipposgrumm.corrosive_sculk.util.BrewHelper;
import dev.hipposgrumm.corrosive_sculk.util.NetworkHelper;
import dev.hipposgrumm.corrosive_sculk.util.SculkDamaging;
import dev.hipposgrumm.corrosive_sculk.util.SculkDamagingEntitiesManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
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
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CorrosiveSculk.MODID)
public class CorrosiveSculk {
    public static final String MODID = "corrosive_sculk";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<MobEffect> POTION_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, MODID);
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);

    public static final RegistryObject<Block> WOVEN_SCULK = BLOCKS.register("woven_sculk", () -> new Block(Block.Properties.copy(Blocks.SCULK)));
    public static final RegistryObject<Block> WOVEN_SCULK_VEIN = BLOCKS.register("woven_sculk_vein", () -> new MultifaceDoNothingBlock(Block.Properties.copy(Blocks.SCULK_VEIN)));
    public static final RegistryObject<Item> WOVEN_SCULK_ITEM = ITEMS.register("woven_sculk", () -> new BlockItem(WOVEN_SCULK.get(), new Item.Properties()));
    public static final RegistryObject<Item> WOVEN_SCULK_VEIN_ITEM = ITEMS.register("woven_sculk_vein", () -> new BlockItem(WOVEN_SCULK_VEIN.get(), new Item.Properties()));

    public static final RegistryObject<MobEffect> SCULK_RESISTANCE = POTION_EFFECTS.register("sculk_resistance", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x063E44) {});
    public static final RegistryObject<Potion> SCULK_RESISTANCE_POTION = POTIONS.register("sculk_resistance", () -> new Potion(new MobEffectInstance(SCULK_RESISTANCE.get(), 3600)));
    public static final RegistryObject<Potion> STRONG_SCULK_RESISTANCE_POTION = POTIONS.register("strong_sculk_resistance", () -> new Potion(new MobEffectInstance(SCULK_RESISTANCE.get(), 3600, 1)));
    public static final RegistryObject<Potion> STRONGER_SCULK_RESISTANCE_POTION = POTIONS.register("stronger_sculk_resistance", () -> new Potion(new MobEffectInstance(SCULK_RESISTANCE.get(), 3600, 2)));
    public static final RegistryObject<Enchantment> ENCHANTMENT_SCULK_TOLERANCE = ENCHANTMENTS.register("sculk_tolerance", () -> new SculkToleranceEnchantment(Enchantment.Rarity.UNCOMMON, EnchantmentCategory.ARMOR));

    public static final RegistryObject<SoundEvent> HEART_SCULK = SOUND_EVENTS.register("player.heart_sculk", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "player.heart_sculk"), 2.0F));
    public static final RegistryObject<SoundEvent> HEART_SCULK_RETURN = SOUND_EVENTS.register("player.heart_sculk_return", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "player.heart_sculk_return"), 2.0F));
    public static final RegistryObject<SoundEvent> HEART_SCULK_WARN = SOUND_EVENTS.register("player.heart_sculk_warn", () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "player.heart_sculk_warn"), 2.0F));

    public static final RegistryObject<Codec<LootModifierAddSculkToleranceItem>> MODIFIER_ADD_SCULK_TOLERANCE_ITEM = LOOT_MODIFIERS.register("sculk_tolerance_enchantment", LootModifierAddSculkToleranceItem.CODEC);

    public static final TagKey<Block> SCULK_BLOCKS = BlockTags.create(ResourceLocation.fromNamespaceAndPath(MODID, "sculk_blocks"));
    public static final TagKey<EntityType<?>> SCULK_IMMUNE_ENTITIES = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(MODID, "sculk_immune"));

    public static final ResourceKey<DamageType> SCULK_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(MODID, "sculk_damage"));

    private static SculkDamagingEntitiesManager SCULK_DAMAGING_ENTITIES;

    public CorrosiveSculk(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();

        BLOCKS.register(bus);
        ITEMS.register(bus);
        POTION_EFFECTS.register(bus);
        POTIONS.register(bus);
        ENCHANTMENTS.register(bus);
        SOUND_EVENTS.register(bus);
        LOOT_MODIFIERS.register(bus);

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
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BrewingRecipeRegistry.addRecipe(new BrewHelper(Potions.AWKWARD, new ItemStack(Items.SCULK), SCULK_RESISTANCE_POTION.get()));
            BrewingRecipeRegistry.addRecipe(new BrewHelper(SCULK_RESISTANCE_POTION.get(), new ItemStack(Items.GLOWSTONE_DUST), STRONG_SCULK_RESISTANCE_POTION.get()));
            BrewingRecipeRegistry.addRecipe(new BrewHelper(STRONG_SCULK_RESISTANCE_POTION.get(), new ItemStack(Items.GLOWSTONE_DUST), STRONGER_SCULK_RESISTANCE_POTION.get()));
        });

        NetworkHelper.init();
    }

    private static void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(WOVEN_SCULK_ITEM);
            event.accept(WOVEN_SCULK_VEIN_ITEM);
        }
    }

    private static void registerReloadListeners(AddReloadListenerEvent event) {
        SCULK_DAMAGING_ENTITIES = new SculkDamagingEntitiesManager();
        event.addListener(SCULK_DAMAGING_ENTITIES);
    }

    private static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity entity && !entity.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE).isPresent()) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath(MODID, "properties"), new SculkDamageCapability.Provider());
        }
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(SculkDamageCapability.class);
    }

    private static void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE).ifPresent(sculkDamage ->
                NetworkHelper.send(player, SculkDamageSyncPacket.update(player, sculkDamage))
            );
        }
    }

    public static void onPlayerRespawn(PlayerEvent.Clone event) {
        if(event.isWasDeath() && event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE).ifPresent(sculkDamage ->
                    NetworkHelper.send(player, SculkDamageSyncPacket.remove(event.getOriginal()))
            );
        }
    }

    private static void startTrackingEntity(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            event.getTarget().getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE).ifPresent(sculkDamage ->
                NetworkHelper.send(player, SculkDamageSyncPacket.update(event.getTarget(), sculkDamage))
            );
        }
    }

    private static void stopTrackingEntity(PlayerEvent.StopTracking event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NetworkHelper.send(player, SculkDamageSyncPacket.remove(event.getTarget()));
        }
    }

    private static void tickAllEntities(TickEvent.LevelTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.level instanceof ServerLevel level) {
            for (Entity e:level.getAllEntities()) {
                if (!(e instanceof LivingEntity entity)) continue;
                entity.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE).ifPresent(sculkDamage -> {
                    boolean updateFull = false, updateWarn = false;
                    boolean damaged;
                    if (SculkDamaging.canSculkDamage(entity)) {
                        boolean isPlayer = entity instanceof ServerPlayer || entity.getControllingPassenger() instanceof ServerPlayer;
                        // Damaging
                        boolean noContactDamaging = isPlayer && !entity.onGround() && sculkDamage.getDamageTimer() != null;
                        damaged = noContactDamaging || (isPlayer ?
                                SculkDamaging.detectSculkCollisionPlayer(entity, level) : // For players do more complex stuff.
                                SculkDamaging.detectSculkCollision(entity, level)         // For other entities simpler checks will do.
                        );
                        if (damaged) {
                            boolean upd = SculkDamaging.handleSculkPresence(sculkDamage, entity, level, noContactDamaging);
                            if (upd) updateFull = true;
                            else updateWarn = true;
                        }
                    } else damaged = false;

                    // Healing
                    if (SculkDamaging.handleSafeAndShouldHeal(sculkDamage, entity, level, damaged)) {
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
    }

    private static void onEntityDamaged(LivingHurtEvent event) {
        Entity attacker = event.getSource().getDirectEntity();
        LivingEntity entity = event.getEntity();
        if (attacker!=null && SculkDamaging.canSculkDamage(entity)) {
            int damage = SCULK_DAMAGING_ENTITIES.getEntitySculkDamage(attacker);
            if (damage>0) entity.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE).ifPresent(sculkDamage -> {
                SculkDamaging.doSculkDamage((int)Math.min(event.getAmount(),damage)/2, sculkDamage, entity, attacker);
                SculkDamaging.sendUpdate(sculkDamage, entity);
            });
        }
    }

    private static void onTotemUsed(LivingUseTotemEvent event) {
        LivingEntity entity = event.getEntity();
        entity.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE).ifPresent(sculkDamage -> {
            float remaining = event.getEntity().getMaxHealth() - (sculkDamage.getDamage()*2);
            sculkDamage.decreaseDamage(1);
            sculkDamage.setForcedHealing(4-(int)remaining);
            sculkDamage.setHealTimer(10);
            if (sculkDamage.getDamageTimer() != null) {
                sculkDamage.setDamageTimer(80);
                sculkDamage.setWarning(100);
                event.getEntity().addEffect(new MobEffectInstance(SCULK_RESISTANCE.get(), 300));
            }
            if (entity instanceof ServerPlayer player) NetworkHelper.send(player, SculkDamageSyncPacket.warning(entity, sculkDamage.getWarning()));
        });
    }
}