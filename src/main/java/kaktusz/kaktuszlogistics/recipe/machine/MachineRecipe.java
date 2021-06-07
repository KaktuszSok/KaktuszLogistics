package kaktusz.kaktuszlogistics.recipe.machine;

import kaktusz.kaktuszlogistics.recipe.CustomRecipe;
import kaktusz.kaktuszlogistics.recipe.ingredients.IRecipeIngredient;
import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.recipe.outputs.IRecipeOutput;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

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
		ConsumptionAftermath aftermath = new ConsumptionAftermath(inputs);

		for (IRecipeIngredient ingredient : ingredients) {
			if(!aftermath.consume(ingredient)) {
				return false; //recipe failed
			}
		}

		return true;
	}

	//CONSUMPTION
	public static class ConsumptionAftermath {
		private final Map<IRecipeInput, Integer> consumed = new HashMap<>();
		private final IRecipeInput[] original;
		private final IRecipeInput[] runningState;

		public ConsumptionAftermath(IRecipeInput[] input) {
			this.original = input;
			this.runningState = IRecipeInput.cloneArray(input);
		}

		/**
		 * Tries to match the given ingredient against the cloned internal state of the input. If it matches, modifies said state accordingly.
		 * @return True if the given ingredient matched any of the non-depleted inputs
		 */
		public boolean consume(IRecipeIngredient consumer) {
			for(int i = 0; i < runningState.length; i++) {
				int amountConsumed = IRecipeIngredient.tryConsume(runningState[i], consumer);
				if(amountConsumed == 0)
					continue; //didn't match

				//otherwise, matched!
				IRecipeInput consumedClone = original[i].clone(amountConsumed);

			}

			return false;
		}

		/**
		 * Applies the changes to the original inputs, modifying them in the real world
		 * @return Cloned inputs, reflecting what inputs were consumed and how much of them was
		 */
		public List<IRecipeInput> applyToOriginal() {
			List<IRecipeInput> result = new ArrayList<>();

			for(Map.Entry<IRecipeInput, Integer> entry : consumed.entrySet()) {
				result.add(entry.getKey().clone(entry.getValue())); //add consumed input to result
				entry.getKey().reduce(entry.getValue()); //reduce original input by amount consumed
			}

			return result;
		}
	}

	/**
	 * @param inputs The inputs to the recipe. These will not be modified.
	 * @return The aftermath of the consumption, which can be applied to the world using applyToOriginal(). Null if recipe failed.
	 */
	public ConsumptionAftermath consumeInputs(IRecipeInput[] inputs) {
		ConsumptionAftermath aftermath = new ConsumptionAftermath(inputs);

		for (IRecipeIngredient ingredient : ingredients) {
			if(!aftermath.consume(ingredient)) {
				return null; //recipe failed
			}
		}

		return aftermath;
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
