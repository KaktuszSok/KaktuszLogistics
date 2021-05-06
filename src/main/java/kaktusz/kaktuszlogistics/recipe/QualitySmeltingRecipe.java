package kaktusz.kaktuszlogistics.recipe;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.properties.ItemQuality;
import kaktusz.kaktuszlogistics.recipe.ingredients.CustomItemIngredient;
import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.recipe.inputs.ItemInput;
import kaktusz.kaktuszlogistics.recipe.outputs.ItemOutput;
import kaktusz.kaktuszlogistics.util.ListUtils;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class QualitySmeltingRecipe extends SmeltingRecipe {

	/**
	 * Both recipe and output must be based upon CustomItems with an ItemQuality property.
	 */
	public QualitySmeltingRecipe(CustomItemIngredient recipe, ItemOutput output) {
		super(recipe, output);
	}

	@Override
	public List<? extends ItemOutput> getOutputs(IRecipeInput... inputs) {
		IRecipeInput first = IRecipeInput.getFirstNonNull(inputs);

		if(recipe.match(first)) { //match! copy quality from input to output
			ItemStack inStack = ((ItemInput)inputs[0]).stack;
			ItemQuality qIn = CustomItem.getFromStack(inStack).findProperty(ItemQuality.class);
			float quality = qIn.getQuality(inStack);

			ItemStack outStack = output.getStack();
			ItemQuality qOut = CustomItem.getFromStack(outStack).findProperty(ItemQuality.class);
			qOut.setQuality(outStack, quality);
			return ListUtils.listFromSingle(new ItemOutput(outStack));
		}
		return null;
	}
}
