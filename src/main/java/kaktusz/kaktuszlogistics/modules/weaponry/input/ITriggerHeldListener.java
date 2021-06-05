package kaktusz.kaktuszlogistics.modules.weaponry.input;

import org.bukkit.inventory.ItemStack;

public interface ITriggerHeldListener {
	void onTriggerHeld(PlayerTriggerHeldEvent e, ItemStack stack);
}
