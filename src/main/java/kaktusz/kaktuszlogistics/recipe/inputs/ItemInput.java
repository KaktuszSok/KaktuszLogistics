package kaktusz.kaktuszlogistics.recipe.inputs;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemInput implements IRecipeInput {

	public final ItemStack stack;

	public ItemInput(ItemStack stack) {
		this.stack = stack;
	}

	@Override
	public boolean isNull() {
		return stack == null || stack.getAmount() == 0 || stack.getType() == Material.AIR;
	}

	public static ItemInput[] fromStackArray(ItemStack[] items) {
		ItemInput[] result = new ItemInput[items.length];
		for(int i = 0; i < items.length; i++) {
			result[i] = new ItemInput(items[i]);
		}
		return result;
	}

	@Override
	public String toString() {
		if(stack == null)
			return "null";
		return stack.getType().name();
	}
}
