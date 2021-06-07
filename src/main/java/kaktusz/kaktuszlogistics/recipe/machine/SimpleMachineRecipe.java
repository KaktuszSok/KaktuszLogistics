package kaktusz.kaktuszlogistics.recipe.machine;

import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.recipe.outputs.IRecipeOutput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleMachineRecipe<OutputType extends IRecipeOutput> extends MachineRecipe<OutputType> {

	protected final List<OutputType> outputs = new ArrayList<>();

	//SETUP
	/**
	 * @param id   Used to uniquely identify this recipe
	 * @param name Display name of this recipe
	 * @param time How long, in ticks the recipe takes to complete
	 */
	public SimpleMachineRecipe(String id, String name, int time) {
		super(id, name, time);
	}

	@SafeVarargs
	public final SimpleMachineRecipe<OutputType> addOutputs(OutputType... outputs) {
		this.outputs.addAll(Arrays.asList(outputs));

		return this;
	}

	//OVERRIDES
	@Override
	protected List<? extends OutputType> getOutputs(IRecipeInput... inputs) {
		if(!checkIfInputsMatch(inputs))
			return null;

		return outputs;
	}

	@Override
	protected List<String> getOutputNames() {
		return outputs.stream().map(IRecipeOutput::getName).collect(Collectors.toList());
	}
}
