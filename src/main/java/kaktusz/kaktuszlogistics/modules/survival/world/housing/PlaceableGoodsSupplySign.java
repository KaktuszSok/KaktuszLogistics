package kaktusz.kaktuszlogistics.modules.survival.world.housing;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.properties.multiblock.SupportedBlockProperty;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.inventory.meta.ItemMeta;

public class PlaceableGoodsSupplySign extends SupportedBlockProperty {
	public PlaceableGoodsSupplySign(CustomItem item) {
		super(item);
	}

	@Override
	public GoodsSupplySignBlock createCustomBlock(ItemMeta stackMeta, Location location) {
		return new GoodsSupplySignBlock(this, location, stackMeta);
	}

	@Override
	public boolean verify(Block block) {
		return block.getBlockData() instanceof WallSign;
	}
}
