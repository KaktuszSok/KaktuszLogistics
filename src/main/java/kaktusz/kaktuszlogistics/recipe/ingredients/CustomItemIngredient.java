package kaktusz.kaktuszlogistics.recipe.ingredients;

import kaktusz.kaktuszlogistics.items.CustomItem;
import org.bukkit.inventory.ItemStack;

public class CustomItemIngredient extends ItemIngredient {

	private final CustomItem type;

	public CustomItemIngredient(CustomItem type) {
		this.type = type;
	}

	@Override
	protected boolean matchStack(ItemStack stack) {
		return type.isStackThisType(stack);
	}
}
