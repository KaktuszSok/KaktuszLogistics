package kaktusz.kaktuszlogistics.recipe.ingredients;

import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import org.bukkit.Bukkit;

public interface IRecipeIngredient {

	public static boolean match(IRecipeInput input, IRecipeIngredient ingredient) {
		if(ingredient == null) {
			return IRecipeInput.isNull(input);
		}
		else if(IRecipeInput.isNull(input)) {
			return false;
		}
		return ingredient.match(input);
	}

	boolean match(IRecipeInput input);

}
