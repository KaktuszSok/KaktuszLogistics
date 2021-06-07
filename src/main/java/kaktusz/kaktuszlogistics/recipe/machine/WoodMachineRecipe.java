package kaktusz.kaktuszlogistics.recipe.machine;

import kaktusz.kaktuszlogistics.recipe.ingredients.IRecipeIngredient;
import kaktusz.kaktuszlogistics.recipe.ingredients.WoodIngredient;
import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.recipe.outputs.IRecipeOutput;
import kaktusz.kaktuszlogistics.recipe.outputs.WoodOutput;

import java.util.List;

public class WoodMachineRecipe<OutputType extends IRecipeOutput> extends SimpleMachineRecipe<OutputType> {
	/**
	 * @param id   Used to uniquely identify this recipe.
	 * @param name Display name of this recipe
	 */
	public WoodMachineRecipe(String id, String name, int time) {
		super(id, name, time);
	}

	@Override
	protected List<OutputType> getOutputs(IRecipeInput... inputs) {
		if(!checkIfInputsMatch(inputs))
			return null;
		String woodType = null;
		for (IRecipeIngredient ingredient : ingredients) {
			if(ingredient instanceof WoodIngredient) {
				WoodIngredient wood = ((WoodIngredient) ingredient);
				//assign the detected wood type if we have none
				if(woodType == null) {
					woodType = wood.getLastScannedWoodType();
					continue;
				}
				//else, check for wood conflict
				if(!woodType.equals(wood.getLastScannedWoodType()))
					return null;
			}
		}

		//passed! Assign wood to outputs
		for(IRecipeOutput output : outputs) {
			if(output instanceof WoodOutput) {
				((WoodOutput) output).setWoodType(woodType);
			}
		}

		return outputs;
	}
}
