package kaktusz.kaktuszlogistics.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * An item which comes in many different forms (its material can be altered per-item)
 */
public class PolymorphicItem extends CustomItem {
	public PolymorphicItem(String type, String displayName, Material material) {
		super(type, displayName, material);
	}

	public void setForm(ItemStack stack, Material form) {
		stack.setType(form);
	}

	@Override
	protected void updateDisplay(ItemStack stack) {
		//<don't update material>
		updateDisplayName(stack);
		updateItemLore(stack);
	}
}
