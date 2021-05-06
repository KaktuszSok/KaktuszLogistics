package kaktusz.kaktuszlogistics.recipe;

import kaktusz.kaktuszlogistics.recipe.ingredients.IRecipeIngredient;
import kaktusz.kaktuszlogistics.recipe.ingredients.ItemIngredient;
import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.recipe.outputs.ItemOutput;
import kaktusz.kaktuszlogistics.util.ListUtils;

import java.util.List;

public class CraftingRecipe extends CustomRecipe<ItemOutput> {

	protected final ItemIngredient[][] recipeMatrix;
	protected final ItemOutput output;

	public CraftingRecipe(ItemIngredient[][] recipe, ItemOutput output) {
		this.recipeMatrix = recipe;
		this.output = output;
	}

	/**
	 * Use for representative purposes only!
	 * If you want to actually give this as an item, use getOutputs()
	 */
	public ItemOutput getQuickOutput() {
		return output;
	}

	@Override
	protected boolean quickMatch(IRecipeInput... inputs) {
		//only allow square recipes
		return isSquareNum(inputs.length);
	}

	@Override
	public List<? extends ItemOutput> getOutputs(IRecipeInput... inputs) {
		//we have to support the input grid being larger than the recipe matrix.
		int inputsSideLength = (int)Math.sqrt(inputs.length); //side length of the square produced by arranging the inputs in a square shape
		int wiggleroomX = inputsSideLength - getSizeX();
		int wiggleroomY = inputsSideLength - getSizeY();
		for(int y = 0; y <= wiggleroomX; y++) {
			for(int x = 0; x <= wiggleroomY; x++) {
				List<? extends ItemOutput> outputs = getOutputs(inputsSideLength, x, y, inputs);
				if(outputs != null)
					return outputs;
			}
		}

		return null;
	}

	public ItemIngredient[][] getRecipeMatrix() {
		return recipeMatrix;
	}

	//HELPERS
	protected boolean isSquareNum(int n) {
		return Math.sqrt(n) == Math.floor(Math.sqrt(n));
	}

	/**
	 * Compares the inputs to the recipe matrix with some offset towards the bottom-right and returns the outputs (or null if none)
	 */
	protected List<? extends ItemOutput> getOutputs(int squareSize, int Xoffset, int Yoffset, IRecipeInput... inputs) {
		for(int y = 0; y < squareSize; y++) {
			for(int x = 0; x < squareSize; x++) {
				IRecipeInput input = inputs[squareSize*y + x];
				ItemIngredient target = getIngredientAt(x, y, Xoffset, Yoffset);
				if(!IRecipeIngredient.match(input, target))
					return null;
			}
		}

		//passed check
		return ListUtils.listFromSingle(output);
	}

	protected ItemIngredient getIngredientAt(int x, int y, int Xoffset, int Yoffset) {
		if(withinOffsetBounds(x, y, Xoffset, Yoffset)) {
			return recipeMatrix[y-Yoffset][x-Xoffset];
		}
		else {
			return null;
		}
	}

	/**
	 * @return True if the point x,y is within the bounds of our recipe matrix offset to the bottom-right by some amount.
	 */
	@SuppressWarnings("RedundantIfStatement") //more readable
	protected boolean withinOffsetBounds(int x, int y, int Xoffset, int Yoffset) {
		x = x - Xoffset;
		if(x < 0 || x >= getSizeX()) //OOB x
			return false;
		y = y - Yoffset;
		if(y < 0 || y >= getSizeY()) //OOB y
			return false;

		return true;
	}

	public int getSizeX() {
		return recipeMatrix[0].length;
	}
	public int getSizeY() {
		return recipeMatrix.length;
	}
}
