package kaktusz.kaktuszlogistics.items.properties;

import kaktusz.kaktuszlogistics.items.events.IPlacedListener;
import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.world.CustomBlock;
import kaktusz.kaktuszlogistics.world.KLWorld;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

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

		CustomBlock block = world.setBlock(createCustomBlock(stack.getItemMeta(), b.getLocation()), x,y,z); //set block in KLWorld
		block.onPlaced(e);
	}

	public CustomBlock createCustomBlock(ItemMeta stackMeta, Location location) {
		return new CustomBlock(this, location, stackMeta);
	}

	public boolean verify(Block block) {
		return block.getType() == item.material;
	}
}
