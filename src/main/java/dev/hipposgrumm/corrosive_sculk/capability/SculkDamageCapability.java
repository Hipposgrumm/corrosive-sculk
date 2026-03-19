package dev.hipposgrumm.corrosive_sculk.capability;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.HashMap;
import java.util.Map;

public class SculkDamageCapability {
    public static final Map<Integer, ClientData> ENTITIES = new HashMap<>();

    private int damage;
    private int protection;
    private int maxProtection;
    private byte sculkWarningPercent;
    private int lastContact;
    private Integer damageTimer;
    private int damageTimerMax = -1;
    private Integer healTimer;
    private int forceHeal;

    public int getDamage() {
        return damage;
    }

    public int getProtection() {
        return protection;
    }

    public int getMaxProtection() {
        return maxProtection;
    }

    public byte getWarning() {
        return sculkWarningPercent;
    }

    public long getLastContactCounter() {
        return lastContact;
    }

    public Integer getDamageTimer() {
        return damageTimer;
    }

    public int getDamageTimerMax() {
        return damageTimerMax;
    }

    public Integer getHealTimer() {
        return healTimer;
    }

    public boolean forceHeal() {
        return forceHeal > 0;
    }

    public void increaseDamage(int amount) {
        this.damage += amount;
    }

    public void decreaseDamage(int amount) {
        this.damage = Math.max(damage - amount, 0);
    }

    public void setProtection(int amount) {
        this.protection = Math.min(amount, maxProtection);
    }

    public void setMaxProtection(int amount) {
        this.maxProtection = amount;
    }

    public void healProtection(int amount) {
        this.protection = Math.max(this.protection+amount, this.maxProtection);
    }

    public void damageProtection(int amount) {
        this.protection = Math.max(this.protection-amount, 0);
    }

    public void setWarning(int value) {
        this.sculkWarningPercent = (byte)value;
    }

    public void setLastContactCounter(int time) {
        this.lastContact = time;
    }

    public void decrementContactCounter() {
        if (lastContact > 0) this.lastContact--;
    }

    public void setDamageTimer(Integer time) {
        this.damageTimer = time;
        this.damageTimerMax = time != null ? time : -1;
    }

    public void decrementDamageTimer() {
        this.damageTimer--;
    }

    public void setHealTimer(Integer time) {
        this.healTimer = time;
    }

    public void decrementHealTimer() {
        this.healTimer--;
    }

    public void setForcedHealing(int health) {
        this.forceHeal = health;
    }

    public void consumeForcedHeal() {
        this.forceHeal--;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putInt("damage", damage);
        nbt.putInt("protection", protection);
        nbt.putInt("maxProtection", maxProtection);
        nbt.putByte("sculkWarning", sculkWarningPercent);
        nbt.putLong("lastContact", lastContact);
        if (damageTimer != null) {
            nbt.putInt("damageTimer", damageTimer);
            nbt.putInt("damageTimerMax", damageTimerMax);
        }
        if (healTimer != null) nbt.putInt("healTimer", healTimer);
        nbt.putInt("forceHeal", forceHeal);
    }

    public void loadNBTData(CompoundTag nbt) {
        damage = nbt.getInt("damage");
        protection = nbt.getInt("protection");
        maxProtection = nbt.getInt("maxProtection");
        sculkWarningPercent = nbt.getByte("sculkWarning");
        lastContact = nbt.getInt("lastContact");
        if (nbt.contains("damageTimer")) {
            damageTimer = nbt.getInt("damageTimer");
            damageTimerMax = nbt.getInt("damageTimerMax");
        }
        if (nbt.contains("healTimer")) healTimer = nbt.getInt("healTimer");
        forceHeal = nbt.getInt("forceHeal");
    }

    public static class Provider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        public static Capability<SculkDamageCapability> SCULK_DAMAGE = CapabilityManager.get(new CapabilityToken<>(){});

        private SculkDamageCapability capability = null;
        private final LazyOptional<SculkDamageCapability> optional = LazyOptional.of(this::createSculkDamageCapability);

        private SculkDamageCapability createSculkDamageCapability() {
            if(this.capability == null) this.capability = new SculkDamageCapability();
            return this.capability;
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
            if(capability == SCULK_DAMAGE) return optional.cast();
            return LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag nbt = new CompoundTag();
            createSculkDamageCapability().saveNBTData(nbt);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            createSculkDamageCapability().loadNBTData(nbt);
        }
    }

    public static class ClientData {
        public static final ClientData EMPTY = new ClientData();

        private int damage;
        private int protection;
        private int maxProtection;
        private byte sculkWarningPercent;

        public void set(int damage, int protection, int maxProtection, byte sculkWarning) {
            this.damage = damage;
            this.protection = protection;
            this.maxProtection = maxProtection;
            this.sculkWarningPercent = sculkWarning;
        }

        public void setWarning(byte sculkWarning) {
            this.sculkWarningPercent = sculkWarning;
        }

        public int getDamage() {
            return damage;
        }

        public int getProtection() {
            return protection;
        }

        public int getMaxProtection() {
            return maxProtection;
        }

        public byte getWarning() {
            return sculkWarningPercent;
        }

        public static void playHeartSound(SoundEvent sound) {
            if (FMLEnvironment.dist.isClient()) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forAmbientAddition(sound));
            }
        }
    }
}
