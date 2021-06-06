package kaktusz.kaktuszlogistics.recipe.ingredients;

import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.recipe.inputs.ItemInput;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.List;

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

	public abstract List<Material> getValidInputMaterials();
}
