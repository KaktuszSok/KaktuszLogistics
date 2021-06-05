package kaktusz.kaktuszlogistics.items.events;

import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public interface IPlacedListener {
	void onTryPlace(BlockPlaceEvent e, ItemStack stack);
}
