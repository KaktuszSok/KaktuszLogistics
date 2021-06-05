package kaktusz.kaktuszlogistics.world.multiblock;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.properties.ItemPlaceable;
import kaktusz.kaktuszlogistics.world.KLWorld;
import org.bukkit.block.Block;

public class ComponentCustomBlock extends MultiblockComponent {

	private final ItemPlaceable type;

	public ComponentCustomBlock(ItemPlaceable type) {
		this.type = type;
	}

	/**
	 * @param item The CustomItem which we are matching against, must have ItemPlaceable component.
	 */
	public static ComponentCustomBlock fromCustomItem(CustomItem item) {
		return new ComponentCustomBlock(item.findProperty(ItemPlaceable.class));
	}

	@Override
	public boolean match(Block block, MultiblockBlock multiblock) {
		return KLWorld.get(block.getWorld()).getBlockAt(block.getX(), block.getY(), block.getZ()).type == type;
	}
}
