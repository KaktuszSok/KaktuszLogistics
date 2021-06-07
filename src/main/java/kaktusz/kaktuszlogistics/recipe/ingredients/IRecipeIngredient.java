package kaktusz.kaktuszlogistics.recipe.ingredients;

import kaktusz.kaktuszlogistics.recipe.CustomRecipe;
import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;

import java.util.Map;
import java.util.stream.Stream;

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

	/**
	 * Tries to match, and if it does, consumes the appropriate amount from the given input, modifying it
	 * @return How much was consumed. 0 if the recipe didn't match.
	 */
	public static int tryConsume(IRecipeInput input, IRecipeIngredient ingredient) {
		if(match(input, ingredient)) {
			return ingredient.consume(input);
		}

		return 0;
	}

	/**
	 * Consumes the appropriate amount from the given input, modifying it
	 * @return How much was consumed. 0 if failed to match.
	 */
	int consume(IRecipeInput input);

	String getName();
}
