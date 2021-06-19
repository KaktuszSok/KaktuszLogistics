package kaktusz.kaktuszlogistics.modules.survival.world.housing;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.properties.ItemPlaceable;
import kaktusz.kaktuszlogistics.world.CustomBlock;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Property which places down a house sign
 */
public class PlaceableHouseSign extends ItemPlaceable {
	public PlaceableHouseSign(CustomItem item) {
		super(item);
	}

	@Override
	public CustomBlock createCustomBlock(ItemMeta stackMeta, Location location) {
		return new HouseSignBlock(this, location, stackMeta);
	}

	@Override
	public boolean verify(Block block) {
		return block.getBlockData() instanceof WallSign;
	}
}
