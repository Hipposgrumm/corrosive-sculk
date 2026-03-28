package dev.hipposgrumm.corrosive_sculk.loot;

//? if fabric {
/*import dev.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.function.Consumer;

public class LootModifiers {
    public static void register(ResourceLocation id, LootTable.Builder tableBuilder) {
        if (id.toString().equals("minecraft:chests/ancient_city")) {
            addSculkEnchItem(tableBuilder, Items.ENCHANTED_BOOK, 0.40f);
            addSculkEnchItem(tableBuilder, Items.DIAMOND_CHESTPLATE, 0.06f);
            addSculkEnchItem(tableBuilder, Items.IRON_CHESTPLATE, 0.10f);
            addSculkEnchItem(tableBuilder, Items.DIAMOND_HORSE_ARMOR, 0.12f);
        }
    }

    private static void addSculkEnchItem(LootTable.Builder tableBuilder, Item item, float chance) {
        tableBuilder.pool(new LootPool.Builder()
                .setRolls(ConstantValue.exactly(1))
                .conditionally(LootItemRandomChanceCondition.randomChance(chance).build())
                .with(SculkToleranceLootItem.lootTableItem(item).build())
                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)).build())
                .build()
        );
    }

    private static class SculkToleranceLootItem extends LootPoolSingletonContainer {
        final Item item;

        private SculkToleranceLootItem(Item item, int weight, int quality, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions) {
            super(weight, quality, lootItemConditions, lootItemFunctions);
            this.item = item;
        }

        public LootPoolEntryType getType() {
            return LootPoolEntries.ITEM;
        }

        public void createItemStack(Consumer<ItemStack> out, LootContext ctx) {
            int random = ctx.getRandom().nextInt(10);
            int level;
            if (random >= 5) level = 3;
            else if (random >= 2) level = 2;
            else level = 1;
            ItemStack item = new ItemStack(this.item);
            item.enchant(CorrosiveSculk.ENCHANTMENT_SCULK_TOLERANCE.get(), level);
            out.accept(item);
        }

        public static LootPoolSingletonContainer.Builder<?> lootTableItem(ItemLike item) {
            return simpleBuilder(
                    (weight, quality, conditions, functions) ->
                            new SculkToleranceLootItem(item.asItem(), weight, quality, conditions, functions)
            );
        }
    }
}
*///?}