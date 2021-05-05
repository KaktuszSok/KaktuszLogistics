package kaktusz.kaktuszlogistics.recipe.inputs;

public interface IRecipeInput {

	public static boolean isNull(IRecipeInput input) {
		if(input == null)
			return true;
		return input.isNull();
	}

	boolean isNull();

}
