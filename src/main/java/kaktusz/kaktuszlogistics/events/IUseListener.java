package kaktusz.kaktuszlogistics.events;

import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public interface IUseListener {
	void onTryUse(PlayerInteractEvent e, ItemStack stack);

	void onTryUseEntity(PlayerInteractEntityEvent e, ItemStack stack);
}
