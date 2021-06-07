package kaktusz.kaktuszlogistics.world.multiblock;

import org.bukkit.block.Block;
import org.bukkit.block.Container;

public class ComponentItemContainer extends MultiblockComponent {

	@Override
	public boolean match(Block block, MultiblockBlock multiblock) {
		return block.getState() instanceof Container;
	}
}
