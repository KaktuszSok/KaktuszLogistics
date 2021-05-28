package kaktusz.kaktuszlogistics.modules.weaponry.input;

import kaktusz.kaktuszlogistics.modules.weaponry.input.PlayerTriggerHeldEvent;
import org.bukkit.inventory.ItemStack;

public interface ITriggerHeldListener {
	void onTriggerHeld(PlayerTriggerHeldEvent e, ItemStack stack);
}
