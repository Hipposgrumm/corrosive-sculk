package gg.hipposgrumm.corrosive_sculk.sculk_damage_capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class SculkDamageCapability {
    private int damage;
    private int protection;
    private boolean sculkWarning;

    public int getDamage() {
        return damage;
    }

    public int getProtection() {
        return protection;
    }

    public boolean getWarning() {
        return sculkWarning;
    }

    public void increaseDamage(int amount) {
        this.damage += amount;
    }

    public void decreaseDamage(int amount) {
        this.damage = Math.max(damage - amount, 0);
    }

    public void setProtection(int amount) {
        this.protection = amount;
    }

    // Kind of a sus naming convention, not gonna lie.
    public void removeProtection(int amount) {
        this.protection = Math.max(this.protection-amount, 0);
    }

    public void setWarning(boolean value) {
        this.sculkWarning = value;
    }

    public void copyFrom(SculkDamageCapability source) {
        this.damage = source.damage;
        this.protection = source.protection;
        this.sculkWarning = source.sculkWarning;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putInt("damage", damage);
        nbt.putInt("protection", protection);
        nbt.putBoolean("sculkWarning", sculkWarning);
    }

    public void loadNBTData(CompoundTag nbt) {
        damage = nbt.getInt("damage");
        protection = nbt.getInt("protection");
        sculkWarning = nbt.getBoolean("sculkWarning");
    }

    public static class Provider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        public static Capability<SculkDamageCapability> SCULK_DAMAGE = CapabilityManager.get(new CapabilityToken<>(){});

        private SculkDamageCapability capability = null;
        private final LazyOptional<SculkDamageCapability> optional = LazyOptional.of(this::createSculkDamageCapability);

        private SculkDamageCapability createSculkDamageCapability() {
            if(this.capability == null) {
                this.capability = new SculkDamageCapability();
            }

            return this.capability;
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
            if(capability == SCULK_DAMAGE) {
                return optional.cast();
            }

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
        private static int damage;
        private static int protection;
        private static boolean sculkWarning;

        public static void set(int damage, int protection, boolean sculkWarning) {
            ClientData.damage = damage;
            ClientData.protection = protection;
            ClientData.sculkWarning = sculkWarning;
        }

        public static int getDamage() {
            return damage;
        }

        public static int getProtection() {
            return protection;
        }

        public static boolean getWarning() {
            return sculkWarning;
        }
    }
}
