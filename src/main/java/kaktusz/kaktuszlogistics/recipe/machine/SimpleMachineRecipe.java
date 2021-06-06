package kaktusz.kaktuszlogistics.recipe.machine;

import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.recipe.outputs.IRecipeOutput;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleMachineRecipe<OutputType extends IRecipeOutput> extends MachineRecipe<OutputType> {

	private final List<OutputType> outputs = new ArrayList<>();

	/**
	 * @param id   Used to uniquely identify this recipe.
	 * @param name Display name of this recipe
	 */
	public SimpleMachineRecipe(String id, String name) {
		super(id, name);
	}

	@Override
	protected List<? extends OutputType> getOutputs(IRecipeInput... inputs) {
		return outputs;
	}

	@SafeVarargs
	public final SimpleMachineRecipe<OutputType> addOutputs(OutputType... outputs) {
		this.outputs.addAll(Arrays.asList(outputs));

		return this;
	}

	@Override
	protected List<String> getOutputNames() {
		return outputs.stream().map(OutputType::getName).collect(Collectors.toList());
	}
}
