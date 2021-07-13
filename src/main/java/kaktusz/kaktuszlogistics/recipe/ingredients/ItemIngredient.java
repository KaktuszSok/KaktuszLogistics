package kaktusz.kaktuszlogistics.recipe.ingredients;

import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.recipe.inputs.ItemInput;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public abstract class ItemIngredient implements IRecipeIngredient {

	public final int amount;

	public ItemIngredient(int amount) {
		this.amount = amount;
	}

	@Override
	public final boolean match(IRecipeInput input) {
		if(input instanceof ItemInput)
			return matchStack(((ItemInput)input).stack);
		return false;
	}

	protected abstract boolean matchStack(ItemStack stack);

	@Override
	public int consume(IRecipeInput input) {
		input.reduce(amount);
		return amount;
	}

	public abstract Set<Material> getValidInputMaterials();
}
