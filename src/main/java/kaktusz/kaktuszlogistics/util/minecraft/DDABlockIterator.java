package kaktusz.kaktuszlogistics.util.minecraft;

import kaktusz.kaktuszlogistics.util.DDARaycast;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Iterator;

/**
 * Iterates over all blocks which intersect a line segment using the DDA algorithm.
 */
public class DDABlockIterator implements Iterator<Block> {

	private final World world;
	private final boolean ignoreLiquids;
	private final boolean ignorePassables;

	private final DDARaycast raycast;
	private Block nextBlock;

	/**
	 * @param start Start of line segment
	 * @param end End of line segment
	 * @param ignoreLiquids should liquids be ignored?
	 * @param ignorePassables should air and other passable blocks be ignored?
	 */
	public DDABlockIterator(World world, Vector start, Vector end, boolean ignoreLiquids, boolean ignorePassables) {
		this.world = world;
		this.ignoreLiquids = ignoreLiquids;
		this.ignorePassables = ignorePassables;
		raycast = new DDARaycast(start, end);
		precalcNextBlock();
	}

	@Override
	public boolean hasNext() {
		return nextBlock != null;
	}

	@Override
	public Block next() {
		Block b = nextBlock;
		precalcNextBlock();
		return b;
	}

	private void precalcNextBlock() {
		nextBlock = null;
		//step through hit coords and find the first valid block
		while(nextBlock == null) {
			Vector pos = raycast.nextStep();
			if(pos == null) //exhausted all steps
				return;
			Block b = world.getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());

			if(ignorePassables && b.isPassable())
				continue;
			if(ignoreLiquids && b.isLiquid())
				continue;

			nextBlock = b;
		}
	}
}
