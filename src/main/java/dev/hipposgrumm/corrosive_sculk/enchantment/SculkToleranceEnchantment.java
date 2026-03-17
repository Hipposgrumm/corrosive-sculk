package dev.hipposgrumm.corrosive_sculk.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class SculkToleranceEnchantment extends Enchantment {
    public SculkToleranceEnchantment(Rarity rarity, EnchantmentCategory category) {
        super(rarity, category, new EquipmentSlot[] {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        });
    }

    @Override
    public boolean canEnchant(ItemStack item) {
        if (item.getItem() instanceof HorseArmorItem) return true;
        // TODO: Wold armor
        return super.canEnchant(item);
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}
