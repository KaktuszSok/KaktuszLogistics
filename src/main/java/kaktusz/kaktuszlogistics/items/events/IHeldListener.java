package kaktusz.kaktuszlogistics.items.events;

import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

public interface IHeldListener {
	@SuppressWarnings("unused")
	void onHeld(PlayerItemHeldEvent e, ItemStack stack);
}
