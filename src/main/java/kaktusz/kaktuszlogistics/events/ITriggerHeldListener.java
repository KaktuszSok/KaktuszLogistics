package kaktusz.kaktuszlogistics.events;

import kaktusz.kaktuszlogistics.events.input.PlayerTriggerHeldEvent;
import org.bukkit.inventory.ItemStack;

public interface ITriggerHeldListener {
	void onTriggerHeld(PlayerTriggerHeldEvent e, ItemStack stack);
}
