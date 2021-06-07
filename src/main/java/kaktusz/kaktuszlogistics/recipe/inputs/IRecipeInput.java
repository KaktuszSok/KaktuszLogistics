package kaktusz.kaktuszlogistics.recipe.inputs;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public interface IRecipeInput extends ConfigurationSerializable {

	public static boolean isNull(IRecipeInput input) {
		if(input == null)
			return true;
		return input.isNull();
	}

	boolean isNull();

	public static IRecipeInput getFirstNonNull(IRecipeInput... inputs) {
		for(IRecipeInput in : inputs) { //get first non-null input
			if(!isNull(in)) {
				return in;
			}
		}
		return null;
	}

	IRecipeInput clone();

	/**
	 * Clones and sets the clone's amount to a specified value
	 */
	IRecipeInput clone(int newAmount);

	/**
	 * Consumes a certain amount of the input that this object is representing
	 */
	void reduce(int amount);

	public static IRecipeInput[] cloneArray(IRecipeInput[] original) {
		IRecipeInput[] cloned = new IRecipeInput[original.length];
		for(int i = 0; i < original.length; i++) {
			cloned[i] = original[i].clone();
		}
		return cloned;
	}
}
