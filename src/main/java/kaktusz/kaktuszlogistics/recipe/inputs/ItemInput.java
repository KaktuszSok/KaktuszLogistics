package kaktusz.kaktuszlogistics.recipe.inputs;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemInput implements IRecipeInput {

	public final ItemStack stack;

	public ItemInput(ItemStack stack) {
		this.stack = stack;
	}

	@Override
	public boolean isNull() {
		return stack == null || stack.getAmount() == 0 || stack.getType() == Material.AIR;
	}

	public static List<ItemInput> fromStackArray(ItemStack[] items) {
		List<ItemInput> result = new ArrayList<>();
		for(ItemStack i : items) {
			result.add(new ItemInput(i));
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
