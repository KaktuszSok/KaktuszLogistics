package kaktusz.kaktuszlogistics.events;

import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

public interface IHeldListener {
	void onHeld(PlayerItemHeldEvent e, ItemStack stack);
}
