package dev.hipposgrumm.corrosive_sculk.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Ingredient;
//? if forge {
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
//?} else {
/*import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistry;
*///?}

public class BrewHelper {
    public static void createRecipes(Potion base, ItemStack ingredient, Potion result) {
        //? if fabric {
        /*FabricBrewingRecipeRegistry.registerPotionRecipe(base, Ingredient.of(ingredient), result);
        *///?} else {
        BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), base)),
                Ingredient.of(ingredient),
                PotionUtils.setPotion(new ItemStack(Items.POTION), result)
        );
        BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), base)),
                Ingredient.of(ingredient),
                PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), result)
        );
        BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), base)),
                Ingredient.of(ingredient),
                PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), result)
        );
        //?}
    }
}
