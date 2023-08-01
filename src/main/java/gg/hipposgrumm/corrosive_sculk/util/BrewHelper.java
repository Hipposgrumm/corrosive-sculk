package gg.hipposgrumm.corrosive_sculk.util;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.common.brewing.IBrewingRecipe;

public class BrewHelper implements IBrewingRecipe {
    private final Potion base;
    private final ItemStack ingredient;
    private final Potion result;

    public BrewHelper(Potion base, ItemStack ingredient, Potion result) {
        this.base = base;
        this.ingredient = ingredient;
        this.result = result;
    }

    @Override
    public boolean isInput(ItemStack input) {
        return (PotionUtils.getPotion(input) == this.base);
    }

    @Override
    public boolean isIngredient(ItemStack ingredient) {
        return ingredient.getItem() == this.ingredient.getItem();
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        return (isInput(input) && isIngredient(ingredient)) ? PotionUtils.setPotion(input.copy(), this.result) : ItemStack.EMPTY;
    }
}
