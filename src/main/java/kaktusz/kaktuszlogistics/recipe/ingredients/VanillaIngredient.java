package kaktusz.kaktuszlogistics.recipe.ingredients;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.util.ListUtils;
import kaktusz.kaktuszlogistics.util.SetUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;

public class VanillaIngredient extends ItemIngredient {

	private final Material type;

	public VanillaIngredient(Material type) {
		this(type,1);
	}
	public VanillaIngredient(Material type, int amount) {
		super(amount);
		this.type = type;
	}

	@Override
	protected boolean matchStack(ItemStack stack) {
		return stack.getType() == type && stack.getAmount() >= amount && CustomItem.getFromStack(stack) == null;
	}

	@Override
	public Set<Material> getValidInputMaterials() {
		return SetUtils.setFromElements(type);
	}

	@Override
	public String getName() {
		//replace "ITEM_NAME" with "Item Name"
		String[] words = type.name().split("_");
		StringJoiner name = new StringJoiner(" ");
		for (String word : words) {
			String capitalisedWord = word.substring(0, 1);
			if(word.length() > 1)
				capitalisedWord += word.substring(1).toLowerCase();
			name.add(capitalisedWord);
		}

		return name.toString() + " x" + amount;
	}
}
