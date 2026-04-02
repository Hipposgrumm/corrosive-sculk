package dev.hipposgrumm.corrosive_sculk.enchantment;

import dev.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

//? if >=1.20.5 {
/*import net.minecraft.world.item.AnimalArmorItem;
*///?} else {
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
//?}

public class SculkToleranceEnchantment extends Enchantment {
    public SculkToleranceEnchantment() {
        //? if >=1.20.5 {
        /*super(Enchantment.definition(
                CorrosiveSculk.SCULK_TOLERANCE_ENCHANTABLE, 1, 3,
                dynamicCost(1, 10), dynamicCost(6, 10), 2,
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        ));
        *///?} else {
        super(Rarity.UNCOMMON, EnchantmentCategory.ARMOR, new EquipmentSlot[] {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        });
        //?}
    }

    @Override
    public boolean canEnchant(ItemStack item) {
        if (item.getItem() instanceof /*? if >=1.20.5 {*//*AnimalArmorItem*//*?} else {*/HorseArmorItem/*?}*/) return true;
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

    //? if <1.20.5 {
    @Override
    public int getMaxLevel() {
        return 3;
    }
    //?}
}
