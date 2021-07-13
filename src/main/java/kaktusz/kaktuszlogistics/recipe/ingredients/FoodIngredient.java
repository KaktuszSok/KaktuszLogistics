package kaktusz.kaktuszlogistics.recipe.ingredients;

import kaktusz.kaktuszlogistics.util.SetUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class FoodIngredient extends ItemIngredient {
	public enum FoodCategory {
		T1("Tier 1 Food (Unprepared Fruit/Veg)"),
		T2("Tier 2 Food (Simple Prepared Food)"),
		T3("Tier 3 Food (Cooked Food)"),
		T4("Tier 4 Food (Large Meals)"),
		DELICACIES("Delicacies");
		;

		public final String name;
		FoodCategory(String name) {
			this.name = name;
		}
	}
	private final FoodCategory category;

	public FoodIngredient(FoodCategory category, int amount) {
		super(amount);
		this.category = category;
	}

	@Override
	public String getName() {
		return category.name;
	}

	@Override
	protected boolean matchStack(ItemStack stack) {
		return stack.getAmount() >= amount && getValidInputMaterials().contains(stack.getType());
	}

	@Override
	public Set<Material> getValidInputMaterials() {
		switch (category) {
			case T1:
				return SetUtils.setFromElements(
						Material.POTATO,
						Material.CARROT,
						Material.BEETROOT,
						Material.MELON,
						Material.APPLE
				);
			case T2:
				return SetUtils.setFromElements(
						Material.BREAD,
						Material.MUSHROOM_STEW,
						Material.BEETROOT_SOUP
				);
			case T3:
				return SetUtils.setFromElements(
						Material.BAKED_POTATO,
						Material.COOKED_BEEF,
						Material.COOKED_CHICKEN,
						Material.COOKED_COD,
						Material.COOKED_MUTTON,
						Material.COOKED_PORKCHOP,
						Material.COOKED_RABBIT,
						Material.COOKED_SALMON
				);
			case T4:
				return SetUtils.setFromElements(
						Material.PUMPKIN_PIE,
						Material.RABBIT_STEW,
						Material.CAKE
				);
			case DELICACIES:
				return SetUtils.setFromElements(
						Material.COOKIE,
						Material.HONEY_BOTTLE,
						Material.SWEET_BERRIES
				);
			default:
				return SetUtils.setFromElements();
		}

	}
}
