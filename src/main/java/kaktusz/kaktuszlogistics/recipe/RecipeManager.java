package kaktusz.kaktuszlogistics.recipe;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.properties.ItemQuality;
import kaktusz.kaktuszlogistics.recipe.ingredients.CustomItemIngredient;
import kaktusz.kaktuszlogistics.recipe.ingredients.ItemIngredient;
import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.recipe.inputs.ItemInput;
import kaktusz.kaktuszlogistics.recipe.machine.MachineRecipe;
import kaktusz.kaktuszlogistics.recipe.outputs.ItemOutput;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeManager {
	private static final List<CraftingRecipe> craftingTableRecipes = new ArrayList<>();
	private static final List<SmeltingRecipe> furnaceRecipes = new ArrayList<>();
	private static final Map<String, MachineRecipe<?>> machineRecipes = new HashMap<>();
	private static final Map<List<? extends CustomRecipe<?>>, CustomRecipe<?>> recipesCache = new HashMap<>(); //cache last used recipe from each list

	//CRAFTING
	public static void addCraftingRecipe(CraftingRecipe r) {
		craftingTableRecipes.add(r);
	}
	public static CraftingRecipe matchCraftingRecipe(ItemInput... inputs) {
		return matchInputsToRecipeList(craftingTableRecipes, inputs);
	}

	//SMELTING
	/**
	 * @param keySuffix required to ensure no duplication for furnace recipe keys. e.g. "steelDust_1xsteel" for a recipe that smelts steel dust into 1 steel ingot.
	 */
	public static void addSmeltingRecipe(ItemIngredient in, ItemOutput out, String keySuffix, float xp, int cookingTime) {
		addSmeltingRecipe(new SmeltingRecipe(in, out), keySuffix, xp, cookingTime);
	}
	/**
	 * @param keySuffix required to ensure no duplication for furnace recipe keys
	 */
	public static void addSmeltingRecipe(SmeltingRecipe r, String keySuffix, float xp, int cookingTime) {
		furnaceRecipes.add(r);
		//add furnace recipe so stuff starts burning. We will intercept the output.
		ItemStack output = r.getQuickOutput().getStack();
		RecipeChoice.MaterialChoice choice = new RecipeChoice.MaterialChoice(r.recipe.getValidInputMaterials());
		NamespacedKey key = new NamespacedKey(KaktuszLogistics.INSTANCE, "smelt_" + keySuffix);
		FurnaceRecipe furnaceRecipe = new FurnaceRecipe(key, output, choice, xp, cookingTime);
		Bukkit.addRecipe(furnaceRecipe);
	}
	public static void addQualitySmeltingRecipe(CustomItemIngredient in, ItemOutput out, String keySuffix, float xp, int cookingTime) {
		addSmeltingRecipe(new QualitySmeltingRecipe(in, out), keySuffix, xp, cookingTime);
	}
	public static SmeltingRecipe matchSmeltingRecipe(ItemInput... inputs) {
		return matchInputsToRecipeList(furnaceRecipes, inputs);
	}

	public static void addMachineRecipe(MachineRecipe<?> r) {
		machineRecipes.put(r.id, r);
	}
	public static MachineRecipe<?> getMachineRecipeById(String id) {
		return machineRecipes.get(id);
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
	private static <R extends CustomRecipe<?>> R matchInputsToRecipeList(List<R> recipeList, IRecipeInput... inputs) {
		//first, check if the cached result is what we're looking for
		CustomRecipe<?> cache = recipesCache.get(recipeList);
		if(cache != null && cache.getOutputsMatching(inputs) != null)
			return (R)cache;
		//if not, iterate through recipe list to try and find a matching one
		for(R r : recipeList) {
			if(r.getOutputsMatching(inputs) != null) { //match!
				recipesCache.put(recipeList, r); //cache result
				return r;
			}
		}
		return null; //none found
	}
}
