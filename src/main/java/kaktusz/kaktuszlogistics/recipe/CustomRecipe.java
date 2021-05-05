package kaktusz.kaktuszlogistics.recipe;

import kaktusz.kaktuszlogistics.recipe.ingredients.IRecipeIngredient;
import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.recipe.outputs.IRecipeOutput;

import java.util.List;

public abstract class CustomRecipe {

	/**
	 * Used to quickly eliminate invalid recipes without more expensive checks.
	 * @return true if the given inputs might be a match, false if it is guaranteed that these inputs dont match.
	 */
	protected boolean quickMatch(List<IRecipeInput> inputs) {
		return true;
	}

	/**
	 * Called after quickMatch passes.
	 * @return The recipe output for a given set of inputs. Null if the inputs are invalid.
	 */
	public abstract List<? extends IRecipeOutput> getOutputs(List<? extends IRecipeInput> inputs);

}
