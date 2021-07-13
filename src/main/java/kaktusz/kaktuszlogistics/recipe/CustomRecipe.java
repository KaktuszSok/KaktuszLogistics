package kaktusz.kaktuszlogistics.recipe;

import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.recipe.outputs.IRecipeOutput;

import java.util.List;

public abstract class CustomRecipe<OutputType extends IRecipeOutput> {

	protected List<? extends OutputType> outputsCache;

	/**
	 * Checks input against quickMatch and, if it passes, calculates the outputs.
	 * @return null if quickMatch fails, otherwise the result of getOutputs(inputs) (may also be null).
	 */
	public final List<? extends OutputType> getOutputsMatching(IRecipeInput... inputs) {
		if(!quickMatch(inputs)) {
			outputsCache = null;
		}
		outputsCache = getOutputs(inputs);
		return outputsCache;
	}

	/**
	 * Should only be used directly after getOutputsMatching() succeeds.
	 * @return same result as the last call of getOutputsMatching() returned.
	 */
	public List<? extends OutputType> getCachedOutputs() {
		return outputsCache;
	}

	/**
	 * Used to quickly eliminate invalid recipes without more expensive checks.
	 * @return true if the given inputs might be a match, false if it is guaranteed that these inputs dont match.
	 */
	protected boolean quickMatch(IRecipeInput... inputs) {
		return true;
	}

	/**
	 * Should only be called after quickMatch passes.
	 * @return The recipe output for a given set of inputs. Null if the inputs are invalid.
	 */
	protected abstract List<? extends OutputType> getOutputs(IRecipeInput... inputs);

}
