package kaktusz.kaktuszlogistics.items.properties;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.events.IPlacedListener;
import kaktusz.kaktuszlogistics.world.CustomBlock;
import kaktusz.kaktuszlogistics.world.KLWorld;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemPlaceable extends ItemProperty implements IPlacedListener {
	public ItemPlaceable(CustomItem item) {
		super(item);
	}

	@Override
	public void onTryPlace(BlockPlaceEvent e, ItemStack stack) {
		Block b = e.getBlockPlaced();
		int x = b.getX();
		int y = b.getY();
		int z = b.getZ();
		KLWorld world = KLWorld.get(b.getWorld());

		CustomBlock block = world.setBlock(createCustomBlock(stack.getItemMeta()), x,y,z); //set block in KLWorld
		block.onPlaced(e);
	}

	public CustomBlock createCustomBlock(ItemMeta stackMeta) {
		return new CustomBlock(this, stackMeta);
	}
}
