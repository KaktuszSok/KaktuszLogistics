package kaktusz.kaktuszlogistics.world.multiblock;

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
