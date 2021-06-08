package kaktusz.kaktuszlogistics.recipe.machine;

import kaktusz.kaktuszlogistics.recipe.CustomRecipe;
import kaktusz.kaktuszlogistics.recipe.ingredients.IRecipeIngredient;
import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.recipe.outputs.IRecipeOutput;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.*;

public abstract class MachineRecipe<OutputType extends IRecipeOutput> extends CustomRecipe<OutputType> {

	public final String id;
	private final String name;
	private Material displayIconMaterial = Material.CRAFTING_TABLE;
	private int displayIconAmount = 1;
	private static final DecimalFormat secondsFormatting = new DecimalFormat("#0.00");

	protected final List<IRecipeIngredient> ingredients = new ArrayList<>();
	public final int time;

	//SETUP
	/**
	 * @param id   Used to uniquely identify this recipe
	 * @param name Display name of this recipe
	 * @param time How long, in ticks the recipe takes to complete
	 */
	public MachineRecipe(String id, String name, int time) {
		this.name = name;
		this.id = id;
		this.time = time;
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
				Integer storedAmount = consumed.get(original[i]);
				if(storedAmount == null) {
					storedAmount = 0;
				}
				storedAmount += amountConsumed;
				consumed.put(original[i], storedAmount);
				return true;
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
		if(inputs == null) return null;
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
	public ItemStack getDisplayIcon() {
		return getDisplayIcon(null);
	}

	/**
	 * @return The display icon, with inputs highlighted depending on if they're missing or present
	 */
	@SuppressWarnings("ConstantConditions")
	public ItemStack getDisplayIcon(IRecipeInput[] givenInputs) {
		ItemStack icon = new ItemStack(displayIconMaterial, displayIconAmount);
		ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + name);
		//lore:
		List<String> lore = new ArrayList<>();
		lore.add(ChatColor.GRAY + "Time: " + getTimeString(time));
		applyInputsLore(lore, givenInputs);
		lore.add(ChatColor.GRAY + "Outputs:");
		for(String outputName : getOutputNames()) {
			lore.add(ChatColor.GRAY + " + " + ChatColor.BLUE + outputName);
		}
		meta.setLore(lore);
		icon.setItemMeta(meta);
		return icon;
	}

	private void applyInputsLore(List<String> lore, IRecipeInput[] givenInputs) {
		lore.add(ChatColor.GRAY + "Inputs:");
		if(givenInputs == null) { //input-nonspecific
			for(IRecipeIngredient ingredient : ingredients) {
				lore.add(ChatColor.GRAY + " - " + ChatColor.BLUE + ingredient.getName());
			}
		}
		else { //highlight inputs
			ConsumptionAftermath aftermath = new ConsumptionAftermath(givenInputs);
			for(IRecipeIngredient ingredient : ingredients) {
				ChatColor ingredientColour = ChatColor.RED;
				if(aftermath.consume(ingredient))
					ingredientColour = ChatColor.GREEN;
				lore.add(ChatColor.GRAY + " - " + ingredientColour + ingredient.getName());
			}
		}
	}

	public static String getTimeString(int t) {
		float sec = t/20f;
		if(sec > 60) {
			int min = (int)sec/60;
			sec = sec % 60;
			return min + ":" + (int)sec + " min.";
		}
		else {
			return secondsFormatting.format(sec) + "s";
		}
	}

	/**
	 * @return The display name of each output
	 */
	protected abstract List<String> getOutputNames();
}
