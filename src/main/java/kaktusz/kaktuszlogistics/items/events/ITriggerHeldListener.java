package kaktusz.kaktuszlogistics.items.events;

import kaktusz.kaktuszlogistics.items.events.input.PlayerTriggerHeldEvent;
import org.bukkit.inventory.ItemStack;

public interface ITriggerHeldListener {
	void onTriggerHeld(PlayerTriggerHeldEvent e, ItemStack stack);
}
