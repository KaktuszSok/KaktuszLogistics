package kaktusz.kaktuszlogistics.recipe.ingredients;

import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.recipe.inputs.ItemInput;
import org.bukkit.inventory.ItemStack;

public abstract class ItemIngredient implements IRecipeIngredient {

	@Override
	public boolean match(IRecipeInput input) {
		if(input instanceof ItemInput)
			return matchStack(((ItemInput)input).stack);
		return false;
	}

	protected abstract boolean matchStack(ItemStack stack);

}
