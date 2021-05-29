package kaktusz.kaktuszlogistics.items.properties;

import kaktusz.kaktuszlogistics.items.CustomItem;
import org.bukkit.block.Barrel;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class PlaceableOpenBarrel extends ItemPlaceable {
	/**
	 * Note: Item's material must be a barrel
	 */
	public PlaceableOpenBarrel(CustomItem item) {
		super(item);
	}

	@Override
	public void onTryPlace(BlockPlaceEvent e, ItemStack stack) {
		super.onTryPlace(e, stack);

		Barrel barrel = (Barrel)e.getBlockPlaced().getState();
		barrel.open();
	}
}
