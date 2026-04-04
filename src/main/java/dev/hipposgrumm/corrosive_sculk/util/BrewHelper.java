package dev.hipposgrumm.corrosive_sculk.util;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Items;
//? if neoforge {
/*import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
*///?} elif forge {
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
//?} else {
    /*//? if >1.20.1 {
    import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
    //?} else {
    /^import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistry;
    ^///?}
*///?}

public class BrewHelper {
    public static void createRecipes(
            //? if >=1.20.5 {
            /*//? if forgebase
            RegisterBrewingRecipesEvent event,
            Holder<Potion> base, Item ingredient, Holder<Potion> result
            *///?} else {
            Potion base, Item ingredient, Potion result
            //?}
    ) {
        //? if fabric {
            /*//? if >=1.20.5 {
            FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> builder.registerPotionRecipe(
                    base, Ingredient.of(ingredient), result
            ));
            //?} else {
            /^FabricBrewingRecipeRegistry.registerPotionRecipe(base, Ingredient.of(ingredient), result);
            ^///?}
        *///?} else {
            //? if >=1.20.5 {
            /*event.getBuilder().addMix(base, ingredient, result);
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
        //?}
    }
}
