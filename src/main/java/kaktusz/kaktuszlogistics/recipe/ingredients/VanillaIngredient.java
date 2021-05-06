package kaktusz.kaktuszlogistics.recipe.ingredients;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.util.ListUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class VanillaIngredient extends ItemIngredient {

	private final Material type;

	public VanillaIngredient(Material type) {
		this.type = type;
	}

	@Override
	protected boolean matchStack(ItemStack stack) {
		return stack.getType() == type && CustomItem.getFromStack(stack) == null;
	}

	@Override
	public List<Material> getValidInputMaterials() {
		return ListUtils.listFromSingle(type);
	}
}
