package kaktusz.kaktuszlogistics.recipe.outputs;

import kaktusz.kaktuszlogistics.recipe.ingredients.WoodIngredient;
import org.bukkit.inventory.ItemStack;

import java.util.StringJoiner;

public class WoodOutput extends ItemOutput {

	private static final String DEFAULT_WOOD_TYPE = "OAK_";
	private final WoodIngredient.WOOD_ITEM item;
	private String woodType = DEFAULT_WOOD_TYPE;

	public WoodOutput(WoodIngredient.WOOD_ITEM itemType, int amount) {
		super(new ItemStack(WoodIngredient.generateMaterial(itemType, DEFAULT_WOOD_TYPE), amount));
		this.item = itemType;
	}

	/**
	 * Prepares the wood type for the next time getStack() is called
	 */
	public void setWoodType(String type) {
		woodType = type;
	}

	@Override
	public ItemStack getStack() {
		ItemStack stack = super.getStack();
		stack.setType(WoodIngredient.generateMaterial(item, woodType));
		return stack;
	}

	@Override
	public String getName() {
		String[] words = item.name().split("_");
		StringJoiner name = new StringJoiner(" ");
		for (String word : words) {
			String capitalisedWord = word.substring(0, 1);
			if(word.length() > 1)
				capitalisedWord += word.substring(1).toLowerCase();
			name.add(capitalisedWord);
		}

		return name.toString();
	}
}
