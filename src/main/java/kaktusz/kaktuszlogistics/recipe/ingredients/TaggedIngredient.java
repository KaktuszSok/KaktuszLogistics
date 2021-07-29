package kaktusz.kaktuszlogistics.recipe.ingredients;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.util.StringUtils;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.Set;

public class TaggedIngredient extends ItemIngredient {
	private final Tag<Material> tag;

	public TaggedIngredient(Tag<Material> tag) {
		this(tag, 1);
	}
	public TaggedIngredient(Tag<Material> tag, int amount) {
		super(amount);
		this.tag = tag;
	}

	@Override
	public String getName() {
		return StringUtils.fixCapitalisation(tag.toString().split("_"));
	}

	@Override
	protected boolean matchStack(ItemStack stack) {
		return stack.getAmount() >= amount && tag.isTagged(stack.getType()) && CustomItem.getFromStack(stack) == null;
	}

	@Override
	public Set<Material> getValidInputMaterials() {
		return tag.getValues();
	}

	@Override
	public RecipeChoice getVanillaRecipeChoice() {
		return new RecipeChoice.MaterialChoice(tag);
	}
}
