package kaktusz.kaktuszlogistics.recipe;

import kaktusz.kaktuszlogistics.recipe.ingredients.ItemIngredient;
import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.recipe.outputs.IRecipeOutput;
import kaktusz.kaktuszlogistics.recipe.outputs.ItemOutput;
import kaktusz.kaktuszlogistics.util.ListUtils;

import java.util.List;

public class SmeltingRecipe extends CustomRecipe<ItemOutput> {

	protected final ItemIngredient recipe;
	protected final ItemOutput output;

	public SmeltingRecipe(ItemIngredient recipe, ItemOutput output) {
		this.recipe = recipe;
		this.output = output;
	}

	/**
	 * Use for representative purposes only!
	 * If you want to actually give this as an item, use getOutputs()
	 */
	public ItemOutput getQuickOutput() {
		return output;
	}

	@SuppressWarnings("RedundantIfStatement") //readability
	@Override
	protected boolean quickMatch(IRecipeInput... inputs) {
		if(inputs.length == 1 && !recipe.match(inputs[0])) {
			return false; //if we only have 1 input and it doesn't match, we are guaranteed to fail
		}
		return true;
	}

	@Override
	public List<? extends ItemOutput> getOutputs(IRecipeInput... inputs) {
		IRecipeInput first = IRecipeInput.getFirstNonNull(inputs);

		if(recipe.match(first)) {
			return ListUtils.listFromSingle(output);
		}
		return null;
	}
}
