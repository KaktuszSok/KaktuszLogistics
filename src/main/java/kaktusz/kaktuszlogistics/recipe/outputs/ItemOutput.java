package kaktusz.kaktuszlogistics.recipe.outputs;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import org.bukkit.inventory.ItemStack;

public class ItemOutput implements IRecipeOutput {

	private final ItemStack itemStack;

	public ItemOutput(ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	public ItemStack getStack() {
		return itemStack.clone();
	}

	@Override
	public String getName() {
		if(itemStack.getItemMeta() != null) {
			if (itemStack.getItemMeta().hasDisplayName()) //use the display name
				return itemStack.getItemMeta().getDisplayName() + " x" + itemStack.getAmount();

			//otherwise, use the localised name
			return itemStack.getItemMeta().getLocalizedName() + " x" + itemStack.getAmount();
		}
		//otherwise, it is air, which is not a valid output
		KaktuszLogistics.LOGGER.warning("Invalid output: " + itemStack.toString());
		return "[INVALID - PLEASE REPORT TO DEVELOPER]";
	}
}
