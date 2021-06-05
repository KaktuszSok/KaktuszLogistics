package kaktusz.kaktuszlogistics.world.multiblock;

import org.bukkit.block.Block;

/**
 * A class which multiblocks use to figure out if a block matches their structure specifications or not
 */
public abstract class MultiblockComponent {
	public abstract boolean match(Block block, MultiblockBlock multiblock);
}
