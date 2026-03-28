package dev.hipposgrumm.corrosive_sculk.util;

import dev.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import dev.hipposgrumm.corrosive_sculk.capability.SculkDamageCapability;
import dev.hipposgrumm.corrosive_sculk.config.Config;
import dev.hipposgrumm.corrosive_sculk.network.SculkDamageSoundPacket;
import dev.hipposgrumm.corrosive_sculk.network.SculkDamageSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// Separated into its own class to avoid spaghetticode.
public class SculkDamaging {
    public static ServerPlayer asPlayer(LivingEntity entity) {
        return entity instanceof ServerPlayer p ? p : null;
    }

    public static boolean detectSculkCollision(LivingEntity entity, LevelAccessor level) {
        AABB box = entity.getBoundingBox();
        for (BlockPos pos : BlockPos.betweenClosed(
                (int) Math.floor(box.minX),
                (int) Math.floor(box.minY-0.0625f),
                (int) Math.floor(box.minZ),
                (int) Math.floor(box.maxX-0.001f),
                (int) Math.floor(box.maxY-0.001f),
                (int) Math.floor(box.maxZ-0.001f)
        )) {
            BlockState state = level.getBlockState(pos);
            if (state.is(CorrosiveSculk.SCULK_BLOCKS))
                return true;
        }
        return false;
    }

    public static boolean detectSculkCollisionPlayer(LivingEntity entity, LevelAccessor level) {
        AABB box = entity.getBoundingBox().inflate(0.0625); // 1/16 block
        for (BlockPos pos : BlockPos.betweenClosed(
                Mth.floor(box.minX),
                Mth.floor(box.minY),
                Mth.floor(box.minZ),
                Mth.floor(box.maxX),
                Mth.floor(box.maxY),
                Mth.floor(box.maxZ)
        )) {
            BlockState state = level.getBlockState(pos);
            if (!state.is(CorrosiveSculk.SCULK_BLOCKS)) continue;

            VoxelShape shape = state.getShape(level, pos, CollisionContext.of(entity));
            for (AABB col : shape.toAabbs()) {
                if (box.intersects(col.move(pos)))
                    return true;
            }
        }
        return false;
    }

    /// @return true if updating all, false if only updating warning
    public static boolean handleSculkPresence(SculkDamageCapability sculkDamage, LivingEntity entity, boolean hasContact, boolean cantBeSafe) {
        ServerPlayer player = asPlayer(entity);
        if (player != null) {
            if (cantBeSafe) sculkDamage.decrementContactCounter();
            if (hasContact) sculkDamage.setLastContactCounter(30);
        }
        if (!sculkDamage.forceHeal() && entity.level().getDifficulty() != Difficulty.PEACEFUL)
            sculkDamage.setHealTimer(null);
        if (sculkDamage.getDamageTimer() == null) {
            sculkDamage.setDamageTimer(getDamageTime(entity));

            //if (player != null) NetworkHelper.send(player, new SculkDamageSoundPacket(SculkDamageSoundPacket.Type.WARN));
            return true;
        } else if (sculkDamage.getDamageTimer() <= 0) {
            if (hasContact) {
                doSculkDamage(1, sculkDamage, entity, null);
                sculkDamage.setDamageTimer(getDamageTime(entity)/2);
                sculkDamage.setWarning(0);
            }
            return true;
        } else {
            sculkDamage.decrementDamageTimer();
            if (sculkDamage.getDamageTimerMax() != 0)
                sculkDamage.setWarning(100-(int) ((sculkDamage.getDamageTimer() / (float) sculkDamage.getDamageTimerMax()) * 100));
            return false;
        }
    }

    public static boolean handleSafeAndShouldHeal(SculkDamageCapability sculkDamage, LivingEntity entity, boolean hasContact, boolean cantBeSafe) {
        ServerPlayer player = asPlayer(entity);
        boolean forceHeal = sculkDamage.forceHeal() || entity.level().getDifficulty() == Difficulty.PEACEFUL;
        boolean safe;
        if (forceHeal) { // Always safe in peaceful.
            safe = true;
        } else {
            safe = !hasContact && (player == null || !cantBeSafe || sculkDamage.getLastContactCounter() <= 0);
            if (safe) sculkDamage.setDamageTimer(null);
        }
        if (safe) sculkDamage.setWarning(0);
        return safe;
    }

    /// @return true if should update, false if not
    public static boolean handleHealing(SculkDamageCapability sculkDamage, LivingEntity entity, LevelAccessor level) {
        boolean forceHeal = sculkDamage.forceHeal() || entity.level().getDifficulty() == Difficulty.PEACEFUL;
        boolean healNormal = (
                    forceHeal || (
                            !Config.sculkHealCircumstance.hasSculkDamage || (Config.sculkHealCircumstance.resistDoesHealing && entity.hasEffect(CorrosiveSculk.SCULK_RESISTANCE.get())) ||
                            level.getBrightness(LightLayer.SKY, entity.blockPosition()) > 4
                    )
                ) && (sculkDamage.getDamage() > 0);
        boolean healResist = sculkDamage.getProtection() < sculkDamage.getMaxProtection();
        boolean healAtAll = (healNormal || healResist) && (forceHeal || !entity.hasEffect(MobEffects.DARKNESS));
        if (healAtAll) {
            if (sculkDamage.getHealTimer() == null) {
                sculkDamage.setHealTimer(40);
            } else if (sculkDamage.getHealTimer() <= 0) {
                if (healNormal) {
                    sculkDamage.decreaseDamage(1);
                    if (sculkDamage.getDamage() > 0) {
                        sculkDamage.consumeForcedHeal();
                    } else {
                        sculkDamage.setForcedHealing(0);
                    }

                    if (Config.sculkHealCircumstance.hasSculkDamage) {
                        ServerPlayer player = asPlayer(entity);
                        if (player != null)
                            NetworkHelper.send(player, new SculkDamageSoundPacket(SculkDamageSoundPacket.Type.HEAL));
                    }
                }
                if (healResist) sculkDamage.setProtection(sculkDamage.getProtection() + 1);
                sculkDamage.setHealTimer(10);
            } else {
                sculkDamage.decrementHealTimer();
            }
            return true;
        } else return false;
    }

    /// @return true if should update, false if no update
    public static boolean handleSculkResistance(SculkDamageCapability sculkDamage, LivingEntity entity) {
        int resistAmount = 0;
        MobEffectInstance sculkResist = entity.getEffect(CorrosiveSculk.SCULK_RESISTANCE.get());
        if (sculkResist != null) resistAmount += sculkResist.getAmplifier()+1;
        // TODO: Deeper and Darker Compat?
        if (resistAmount>0) {
            int difference = Math.max(resistAmount-sculkDamage.getMaxProtection(),0);
            sculkDamage.setMaxProtection(resistAmount);
            sculkDamage.setProtection(sculkDamage.getProtection()+difference);
            return true;
        } else if (sculkDamage.getMaxProtection()>0) {
            sculkDamage.setMaxProtection(0);
            sculkDamage.setProtection(0);
            return true;
        }
        return false;
    }

    public static void sendUpdate(SculkDamageCapability sculkDamage, LivingEntity entity) {
        NetworkHelper.sendTracking(entity, SculkDamageSyncPacket.update(entity, sculkDamage));
    }

    public static void sendUpdateWarn(SculkDamageCapability sculkDamage, LivingEntity entity) {
        ServerPlayer player = asPlayer(entity);
        if (player != null) NetworkHelper.send(player, SculkDamageSyncPacket.warning(entity, sculkDamage.getWarning()));
        for (Entity pass:entity.getPassengers()) {
            if (pass instanceof ServerPlayer pl)
                NetworkHelper.send(pl, SculkDamageSyncPacket.warning(entity, sculkDamage.getWarning()));
        }
    }

    public static boolean canSculkDamage(LivingEntity entity) {             // Entities can't take sculk damage if:
        if (entity.isInvulnerable()) return false;                          // The entity is invulnerable.
        if (entity instanceof ServerPlayer player &&
                ((player.isCreative() || player.isSpectator())))            // Player is in creative mode or spectator.
            return false;
        if (Config.sculkResistInvul)
            if (entity.hasEffect(CorrosiveSculk.SCULK_RESISTANCE.get()))    // The entity has sculk resistance and assist mode is enabled.
                return false;
        return entity.level().getDifficulty() != Difficulty.PEACEFUL &&     // The difficulty is set to peaceful.
                !entity.getType().is(CorrosiveSculk.SCULK_IMMUNE_ENTITIES); // The entity has natural immunity to sculk.
    }

    public static void doSculkDamage(int hearts, SculkDamageCapability sculkDamage, LivingEntity entity, Entity attacker) {
        DamageSource source = new DamageSource(entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(CorrosiveSculk.SCULK_DAMAGE), null, attacker);
        if (Config.sculkHealCircumstance.hasSculkDamage) {
            boolean canDamageStill = true;
            if (sculkDamage.getProtection() > 0) {
                if (hearts > sculkDamage.getProtection()) {
                    hearts -= sculkDamage.getProtection();
                    sculkDamage.setProtection(0);
                } else {
                    sculkDamage.damageProtection(hearts);
                    canDamageStill = false;
                }
            }
            if (canDamageStill && entity.getAbsorptionAmount() > 0) {
                entity.hurt(source, entity.getAbsorptionAmount());
                canDamageStill = false;
            }

            if (canDamageStill) {
                sculkDamage.increaseDamage(hearts);
                float maxHealthAfterSculk = entity.getMaxHealth() - (sculkDamage.getDamage() * 2);
                if (entity.getHealth() > maxHealthAfterSculk) entity.hurt(source, entity.getHealth() - maxHealthAfterSculk);
            }
        } else {
            entity.hurt(source, hearts*2);
        }

        if (entity instanceof ServerPlayer player)
            NetworkHelper.send(player, new SculkDamageSoundPacket(SculkDamageSoundPacket.Type.DAMAGE));
    }

    public static int getDamageTime(LivingEntity entity) {
        int tolerance = 40;
        for (ItemStack armor:entity.getArmorSlots()) {
            int level = EnchantmentHelper.getItemEnchantmentLevel(CorrosiveSculk.ENCHANTMENT_SCULK_TOLERANCE.get(), armor);
            if (armor.getItem() instanceof HorseArmorItem) { // TODO: Dog armor
                tolerance += level*10;
            } else {
                tolerance += level*3;
            }
        }
        return tolerance;
    }
}
