package kaktusz.kaktuszlogistics.recipe;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.properties.ItemQuality;
import kaktusz.kaktuszlogistics.recipe.ingredients.IRecipeIngredient;
import kaktusz.kaktuszlogistics.recipe.ingredients.ItemIngredient;
import kaktusz.kaktuszlogistics.recipe.inputs.IRecipeInput;
import kaktusz.kaktuszlogistics.recipe.inputs.ItemInput;
import kaktusz.kaktuszlogistics.recipe.outputs.ItemOutput;
import kaktusz.kaktuszlogistics.util.ListUtils;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QualityPersistingRecipe extends CraftingRecipe {
	public QualityPersistingRecipe(ItemIngredient[][] recipe, ItemOutput output) {
		super(recipe, output);
	}

	@Override
	protected List<ItemOutput> getOutputs(int squareSize, int Xoffset, int Yoffset, IRecipeInput... inputs) {
		Map<CustomItem, Float> qualities = new HashMap<>();

		for(int y = 0; y < squareSize; y++) {
			for(int x = 0; x < squareSize; x++) {
				IRecipeInput input = inputs[squareSize*y + x];

				//check if quality is consistent among same items
				if(!isInputQualityConsistent(input, qualities))
					return null;

				ItemIngredient target = getIngredientAt(x, y, Xoffset, Yoffset);
				if(!IRecipeIngredient.match(input, target))
					return null;
			}
		}

		//passed check
		ItemOutput result = output;
		//if output has quality, make it average of all the input qualities
		CustomItem outputCustom = CustomItem.getFromStack(output.getStack());
		if(outputCustom != null) {
			ItemQuality outputQ = outputCustom.findProperty(ItemQuality.class);
			if(outputQ != null) {
				float totalQ = 0;
				for(float q : qualities.values()) {
					totalQ += q;
				}
				float avgQ = totalQ / qualities.values().size();
				ItemStack outputStack = output.getStack();
				outputQ.setQuality(outputStack, avgQ);
				result = new ItemOutput(outputStack);
			}
		}
		return ListUtils.listFromSingle(result);
	}

	private boolean isInputQualityConsistent(IRecipeInput input, Map<CustomItem, Float> qualities) {
		if(input instanceof ItemInput) {
			ItemStack stack = ((ItemInput)input).stack;
			CustomItem customItem = CustomItem.getFromStack(stack);
			if(customItem == null)
				return true; //all vanilla items are quality consistent
			ItemQuality qprop = customItem.findProperty(ItemQuality.class);
			if(qprop == null)
				return true; //all non-tiered/non-quality-having items are quality consistent

			float quality = qprop.getQuality(stack);
			if(qualities.containsKey(customItem)) {
				return quality == qualities.get(customItem); //quality is inconsistent!
			} else {
				qualities.put(customItem, quality); //keep track of quality to compare against to-be-inspected itemstacks
			}
		}
		return true;
	}
}
