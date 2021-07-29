package kaktusz.kaktuszlogistics.recipe.ingredients;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.PolymorphicItem;
import kaktusz.kaktuszlogistics.util.SetUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.Set;

public class CustomItemIngredient extends ItemIngredient {

	private final CustomItem type;

	public CustomItemIngredient(CustomItem type) {
		this(type, 1);
	}
	public CustomItemIngredient(CustomItem type, int amount) {
		super(amount);
		this.type = type;
	}

	@Override
	protected boolean matchStack(ItemStack stack) {
		return type.isStackThisType(stack);
	}

	@Override
	public Set<Material> getValidInputMaterials() {
		return SetUtils.setFromElements(type.material);
	}

	@Override
	public RecipeChoice getVanillaRecipeChoice() {
		if(type instanceof PolymorphicItem) {
			return new RecipeChoice.MaterialChoice(Material.values()); //bad on memory but spigot be spigot
		}
		return new RecipeChoice.MaterialChoice(type.material);
	}

	@Override
	public String getName() {
		return type.displayName + " x" + amount;
	}
}
