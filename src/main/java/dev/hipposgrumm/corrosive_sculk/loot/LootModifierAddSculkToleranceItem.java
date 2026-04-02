package dev.hipposgrumm.corrosive_sculk.loot;

//? if forgebase {
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

//? if neoforge {
/*import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
*///?} else {
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
//?}

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class LootModifierAddSculkToleranceItem extends LootModifier {
    private final Item item;
    private final float chance;

    protected LootModifierAddSculkToleranceItem(LootItemCondition[] conditionsIn, Item item, float chance) {
        super(conditionsIn);
        this.item = item;
        this.chance = chance/100f;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.getRandom().nextFloat() <= this.chance) {
            int random = context.getRandom().nextInt(10);
            int level;
            if (random >= 5) level = 3;
            else if (random >= 2) level = 2;
            else level = 1;
            ItemStack item = new ItemStack(this.item);
            item.enchant(CorrosiveSculk.ENCHANTMENT_SCULK_TOLERANCE.get(), level);
            generatedLoot.add(item);
        }
        return generatedLoot;
    }

    public static final Supplier</*? if neoforge {*//*MapCodec*//*?} else {*/Codec/*?}*/<LootModifierAddSculkToleranceItem>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder./*? if neoforge {*//*mapCodec*//*?} else {*/create/*?}*/(inst -> codecStart(inst).and(inst.group(
            /*? if neoforge {*//*BuiltInRegistries.ITEM.byNameCodec*//*?} else {*/ForgeRegistries.ITEMS.getCodec/*?}*/().fieldOf("item").forGetter(m -> m.item),
            Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance)
    )).apply(inst, LootModifierAddSculkToleranceItem::new)));
    @Override
    public /*? if neoforge {*//*MapCodec*//*?} else {*/Codec/*?}*/<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
//?}