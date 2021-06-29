package kaktusz.kaktuszlogistics.world.multiblock.components;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.properties.ItemPlaceable;
import kaktusz.kaktuszlogistics.world.CustomBlock;
import kaktusz.kaktuszlogistics.world.KLWorld;
import kaktusz.kaktuszlogistics.world.multiblock.MultiblockBlock;
import org.bukkit.block.Block;

public class ComponentCustomBlock extends MultiblockComponent {

	private final ItemPlaceable type;

	/**
	 * @param item the CustomItem which we are matching against. Must have ItemPlaceable component.
	 */
	public ComponentCustomBlock(CustomItem item) {
		this(item.findProperty(ItemPlaceable.class));
	}
	public ComponentCustomBlock(ItemPlaceable type) {
		this.type = type;
	}

	@Override
	public boolean match(Block block, MultiblockBlock multiblock) {
		CustomBlock cb = KLWorld.get(block.getWorld()).getBlockAt(block.getX(), block.getY(), block.getZ());
		return cb != null && cb.getType() == type;
	}
}
