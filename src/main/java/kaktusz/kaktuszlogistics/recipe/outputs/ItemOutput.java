package kaktusz.kaktuszlogistics.recipe.outputs;

import org.bukkit.inventory.ItemStack;

public class ItemOutput implements IRecipeOutput {

	private final ItemStack itemStack;

	public ItemOutput(ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	public ItemStack getStack() {
		return itemStack.clone();
	}
}
