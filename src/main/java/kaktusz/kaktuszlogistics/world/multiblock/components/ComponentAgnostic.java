package kaktusz.kaktuszlogistics.world.multiblock.components;

import kaktusz.kaktuszlogistics.world.multiblock.MultiblockBlock;
import org.bukkit.block.Block;

/**
 * Always matches
 */
public class ComponentAgnostic extends MultiblockComponent {
	@Override
	public boolean match(Block block, MultiblockBlock multiblock) {
		return true;
	}
}
