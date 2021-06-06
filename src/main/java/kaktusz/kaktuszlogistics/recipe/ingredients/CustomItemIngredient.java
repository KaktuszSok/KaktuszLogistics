package kaktusz.kaktuszlogistics.recipe.ingredients;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.util.ListUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.List;

public class CustomItemIngredient extends ItemIngredient {

	private final CustomItem type;

	public CustomItemIngredient(CustomItem type) {
		super(1);
		this.type = type;
	}

	@Override
	protected boolean matchStack(ItemStack stack) {
		return type.isStackThisType(stack);
	}

	@Override
	public List<Material> getValidInputMaterials() {
		return ListUtils.listFromSingle(type.material);
	}

	@Override
	public String getName() {
		return type.displayName + " x" + amount;
	}
}
