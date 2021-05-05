package kaktusz.kaktuszlogistics.recipe;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.properties.ItemQuality;
import kaktusz.kaktuszlogistics.recipe.ingredients.CustomItemIngredient;
import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.recipe.inputs.ItemInput;
import kaktusz.kaktuszlogistics.recipe.outputs.ItemOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeManager {
	private static final List<CraftingRecipe> craftingTableRecipes = new ArrayList<>();
	private static final Map<List<? extends CustomRecipe>, CustomRecipe> recipesCache = new HashMap<>(); //cache last used recipe from each list

	public static void addCraftingRecipe(CraftingRecipe r) {
		craftingTableRecipes.add(r);
	}
	public static CraftingRecipe matchCraftingRecipe(List<ItemInput> inputs) {
		return matchInputsToRecipeList(inputs, craftingTableRecipes);
	}

	//HELPER
	public static void addBlockRecipe(CustomItem single, CustomItem block) {
		CustomItemIngredient singleIngredient = new CustomItemIngredient(single);
		CustomItemIngredient blockIngredient = new CustomItemIngredient(block);
		ItemOutput blockOutput = new ItemOutput(block.createStack(1));
		ItemOutput singleOutput = new ItemOutput(single.createStack(9));

		CustomItemIngredient[][] recipeMatrix = new CustomItemIngredient[][] {
				{singleIngredient, singleIngredient, singleIngredient},
				{singleIngredient, singleIngredient, singleIngredient},
				{singleIngredient, singleIngredient, singleIngredient}};
		CustomItemIngredient[][] inverseMatrix = new CustomItemIngredient[][] {{blockIngredient}};

		CraftingRecipe recipe;
		CraftingRecipe inverseRecipe;

		if(single.findProperty(ItemQuality.class) == null) {
			recipe = new CraftingRecipe(recipeMatrix, blockOutput);
			inverseRecipe = new CraftingRecipe(inverseMatrix, singleOutput);
		} else {
			recipe = new QualityPersistingRecipe(recipeMatrix, blockOutput);
			inverseRecipe = new QualityPersistingRecipe(inverseMatrix, singleOutput);
		}

		addCraftingRecipe(recipe);
		addCraftingRecipe(inverseRecipe);
	}

	@SuppressWarnings("unchecked") //as long as we only fill the cache with the proper typed variables, there should be no problems
	private static <R extends CustomRecipe> R matchInputsToRecipeList(List<? extends IRecipeInput> inputs, List<R> recipeList) {
		//first, check if the cached result is what we're looking for
		CustomRecipe cache = recipesCache.get(recipeList);
		if(cache != null && cache.getOutputs(inputs) != null)
			return (R)cache;
		//if not, iterate through recipe list to try and find a matching one
		for(R r : recipeList) {
			if(r.getOutputs(inputs) != null) { //match!
				recipesCache.put(recipeList, r); //cache result
				return r;
			}
		}
		return null; //none found
	}
}
