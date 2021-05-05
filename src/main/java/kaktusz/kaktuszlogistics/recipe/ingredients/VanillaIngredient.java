package kaktusz.kaktuszlogistics.recipe.ingredients;

import kaktusz.kaktuszlogistics.items.CustomItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class VanillaIngredient extends ItemIngredient {

	private final Material type;

	public VanillaIngredient(Material type) {
		this.type = type;
	}

	@Override
	protected boolean matchStack(ItemStack stack) {
		return stack.getType() == type && CustomItem.getFromStack(stack) == null;
	}
}
