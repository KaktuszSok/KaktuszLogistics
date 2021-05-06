package kaktusz.kaktuszlogistics.recipe.inputs;

public interface IRecipeInput {

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

}
