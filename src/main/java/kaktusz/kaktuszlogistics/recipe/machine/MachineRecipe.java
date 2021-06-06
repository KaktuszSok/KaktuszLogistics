package kaktusz.kaktuszlogistics.recipe.machine;

import kaktusz.kaktuszlogistics.recipe.CustomRecipe;
import kaktusz.kaktuszlogistics.recipe.ingredients.IRecipeIngredient;
import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.recipe.outputs.IRecipeOutput;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class MachineRecipe<OutputType extends IRecipeOutput> extends CustomRecipe<OutputType> {

	public final String id;
	private String name;
	private Material displayIconMaterial = Material.CRAFTING_TABLE;
	private int displayIconAmount = 1;

	private final List<IRecipeIngredient> ingredients = new ArrayList<>();

	//SETUP
	/**
	 * @param id Used to uniquely identify this recipe.
	 * @param name Display name of this recipe
	 */
	public MachineRecipe(String id, String name) {
		this.id = id;
	}

	public MachineRecipe<OutputType> addIngredients(IRecipeIngredient... ingredients) {
		this.ingredients.addAll(Arrays.asList(ingredients));

		return this;
	}

	public MachineRecipe<OutputType> setDisplayIcon(Material displayIconMaterial, int displayIconAmount) {
		this.displayIconMaterial = displayIconMaterial;
		this.displayIconAmount = displayIconAmount;
		return this;
	}

	//RECIPE
	public boolean checkIfInputsMatch(IRecipeInput[] inputs) {

	}

	//DISPLAY
	public String getDisplayName() {
		return name;
	}

	/**
	 * @return The itemstack used to display this recipe in a list
	 */
	@SuppressWarnings("ConstantConditions")
	public ItemStack getDisplayIcon() {
		ItemStack icon = new ItemStack(displayIconMaterial, displayIconAmount);
		icon.getItemMeta().setDisplayName(name);
		//lore:
		List<String> lore = new ArrayList<>();
		lore.add(ChatColor.GRAY + "Inputs:");
		for(IRecipeInput input : inputs) {
			lore.add(input)
		}
		icon.getItemMeta().setLore(lore);

		return icon;
	}

	/**
	 * @return The display name of each output
	 */
	protected abstract List<String> getOutputNames();
}
