package gg.hipposgrumm.corrosive_sculk;

import com.mojang.logging.LogUtils;
import gg.hipposgrumm.corrosive_sculk.config.Config;
import gg.hipposgrumm.corrosive_sculk.effects.SculkResistanceEffect;
import gg.hipposgrumm.corrosive_sculk.network.SculkDamageOccurPacket;
import gg.hipposgrumm.corrosive_sculk.network.SculkDamageSyncPacket;
import gg.hipposgrumm.corrosive_sculk.sculk_damage_capability.SculkDamageCapability;
import gg.hipposgrumm.corrosive_sculk.util.BrewHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Mod(CorrosiveSculk.MODID)
public class CorrosiveSculk {
    public static final String MODID = "corrosive_sculk";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<MobEffect> POTION_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);

    public static final RegistryObject<MobEffect> SCULK_RESISTANCE = POTION_EFFECTS.register("sculk_resistance", SculkResistanceEffect::new);
    public static final RegistryObject<Potion> SCULK_RESISTANCE_POTION = POTIONS.register("sculk_resistance", () -> new Potion(new MobEffectInstance(SCULK_RESISTANCE.get(), 3600)));
    public static final RegistryObject<Potion> STRONG_SCULK_RESISTANCE_POTION = POTIONS.register("strong_sculk_resistance", () -> new Potion(new MobEffectInstance(SCULK_RESISTANCE.get(), 3600, 1)));
    public static final RegistryObject<Potion> STRONGER_SCULK_RESISTANCE_POTION = POTIONS.register("stronger_sculk_resistance", () -> new Potion(new MobEffectInstance(SCULK_RESISTANCE.get(), 3600, 2)));

    public static final RegistryObject<SoundEvent> HEART_SCULK = SOUND_EVENTS.register("player.heart_sculk", () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(MODID, "player.heart_sculk"), 2.0F));
    public static final RegistryObject<SoundEvent> HEART_SCULK_RETURN = SOUND_EVENTS.register("player.heart_sculk_return", () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(MODID, "player.heart_sculk_return"), 2.0F));

    private static final Map<String, Integer> damageTimer = new HashMap<>();
    private static final Map<String, Integer> healTimer = new HashMap<>();

    private static SimpleChannel NETWORK_INSTANCE;

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public CorrosiveSculk() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC, "corrosive_sculk.toml");

        POTION_EFFECTS.register(modEventBus);
        POTIONS.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static DamageSource getSculkDamageSource(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(MODID, "sculk_damage"))));
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class CommonEvents {
        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                BrewingRecipeRegistry.addRecipe(new BrewHelper(Potions.AWKWARD, new ItemStack(Items.SCULK), SCULK_RESISTANCE_POTION.get()));
                BrewingRecipeRegistry.addRecipe(new BrewHelper(SCULK_RESISTANCE_POTION.get(), new ItemStack(Items.GLOWSTONE_DUST), STRONG_SCULK_RESISTANCE_POTION.get()));
                BrewingRecipeRegistry.addRecipe(new BrewHelper(STRONG_SCULK_RESISTANCE_POTION.get(), new ItemStack(Items.GLOWSTONE_DUST), STRONGER_SCULK_RESISTANCE_POTION.get()));
            });

            SimpleChannel net = NetworkRegistry.ChannelBuilder
                    .named(new ResourceLocation(MODID, "messages"))
                    .networkProtocolVersion(() -> "1.0")
                    .clientAcceptedVersions(s -> true)
                    .serverAcceptedVersions(s -> true)
                    .simpleChannel();

            NETWORK_INSTANCE = net;

            net.messageBuilder(SculkDamageSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                    .decoder(SculkDamageSyncPacket::new)
                    .encoder(SculkDamageSyncPacket::toBytes)
                    .consumerMainThread(SculkDamageSyncPacket::handle)
                    .add();

            net.messageBuilder(SculkDamageOccurPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                    .decoder(SculkDamageOccurPacket::new)
                    .encoder(SculkDamageOccurPacket::toBytes)
                    .consumerMainThread(SculkDamageOccurPacket::handle)
                    .add();
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeCommonEvents {

        @SubscribeEvent
        public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
            if(event.getObject() instanceof Player) {
                if(!event.getObject().getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE).isPresent()) {
                    event.addCapability(new ResourceLocation(MODID, "properties"), new SculkDamageCapability.Provider());
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerCloned(PlayerEvent.Clone event) {
            if(event.isWasDeath()) {
                event.getOriginal().getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE).ifPresent(oldStore -> {
                    event.getOriginal().getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE).ifPresent(newStore -> {
                        newStore.copyFrom(oldStore);
                    });
                });
            }
        }

        @SubscribeEvent
        public void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.register(SculkDamageCapability.class);
        }

        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if(event.side == LogicalSide.SERVER) {
                Player player = event.player;
                Level level = player.level();
                BlockState state = level.getBlockState(player.blockPosition());
                BlockState state2 = level.getBlockState(player.blockPosition().below());
                String playername = player.getGameProfile().getName();
                player.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE).ifPresent(damageCapability -> {
                    if (!(player.isCreative() || player.isSpectator())) {
                        if (Config.sculk_blocks.contains(state.getBlock()) || Config.sculk_blocks.contains(state2.getBlock())) {
                            damageCapability.setWarning(true);
                            healTimer.remove(playername);
                            if (!damageTimer.containsKey(playername)) {
                                damageTimer.put(playername, 80);
                            } else if (damageTimer.get(playername) <= 0) {
                                damageCapability.setWarning(false);
                                if (damageCapability.getProtection() > 0) {
                                    damageCapability.removeProtection(1);
                                } else if (player.getAbsorptionAmount()>0) {
                                    player.hurt(getSculkDamageSource(level), player.getAbsorptionAmount());
                                } else {
                                    damageCapability.increaseDamage(1);
                                }
                                damageTimer.compute(playername, (key, value) -> 40);
                                NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new SculkDamageOccurPacket(false));
                            } else {
                                damageTimer.compute(playername, (key, value) -> value - 1);
                            }
                        } else if (!(state2.getBlock() instanceof AirBlock)) {
                            boolean isBright = (level.getBrightness(LightLayer.SKY, player.blockPosition()) > 4) && (damageCapability.getDamage() > 0);
                            damageCapability.setWarning(false);
                            damageTimer.remove(playername);
                            if (!healTimer.containsKey(playername)) {
                                healTimer.put(playername, isBright?40:200);
                            } else if (healTimer.get(playername) <= 0) {
                                boolean shouldHealResistHeart = player.hasEffect(SCULK_RESISTANCE.get()) && damageCapability.getDamage() == 0 && (damageCapability.getProtection() < player.getEffect(SCULK_RESISTANCE.get()).getAmplifier()+1);
                                if (isBright) damageCapability.decreaseDamage(1);
                                if (shouldHealResistHeart) damageCapability.setProtection(damageCapability.getProtection()+1);
                                if (isBright || shouldHealResistHeart) healTimer.compute(playername, (key, value) -> 10);
                                if (isBright || shouldHealResistHeart) NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new SculkDamageOccurPacket(true));
                            } else {
                                healTimer.compute(playername, (key, value) -> value - 1);
                            }
                        }
                        if ((((player.getMaxHealth() - (damageCapability.getDamage())*2) < player.getHealth())) && !(damageCapability.getProtection()>0)) {
                            player.hurt(getSculkDamageSource(level), player.getHealth() - (player.getMaxHealth() - (damageCapability.getDamage()*2)));
                        } else if (state2.getBlock() instanceof AirBlock) {
                            damageCapability.setWarning(false);
                        }
                    }

                    NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new SculkDamageSyncPacket(damageCapability.getDamage(), damageCapability.getProtection(), damageCapability.getWarning()));
                });
            }
        }

        // Scrapped Idea
        //@SubscribeEvent // Commmenting this out prevents it from running.
        public static void onPlayerAttacked(LivingAttackEvent event) {
            Entity attacker = event.getSource().getDirectEntity();
            if (event.getEntity() instanceof Player player && attacker!=null) {
                ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(attacker.getType());
                for (String entity:Config.sculk_entities) {
                    if (entity.startsWith(entityId.toString())) {
                        int entitySculkDamage;
                        try {
                            entitySculkDamage = Math.min(Integer.parseInt(entity.replace(" ", "").split(",")[1]), (int) event.getAmount());
                        } catch (NumberFormatException e) {
                            entitySculkDamage = 0;
                        }
                        int finalEntitySculkDamage = entitySculkDamage;
                        LogUtils.getLogger().info("Damage: "+finalEntitySculkDamage);
                        player.getCapability(SculkDamageCapability.Provider.SCULK_DAMAGE).ifPresent(damageCapability -> {
                            if (damageCapability.getProtection() > 0) {
                                damageCapability.removeProtection(finalEntitySculkDamage);
                            } else if (player.getAbsorptionAmount() > 0) {
                                player.hurt(getSculkDamageSource(player.level()), player.getAbsorptionAmount() - event.getAmount());
                            } else {
                                damageCapability.increaseDamage(finalEntitySculkDamage);
                            }
                        });
                    }
                }
            }
        }
    }
}











