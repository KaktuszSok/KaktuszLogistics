package kaktusz.kaktuszlogistics.util;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Iterates over all blocks which intersect a line segment using the DDA algorithm.
 */
public class DDABlockIterator implements Iterator<Block> {

	private final World world;
	private boolean ignoreLiquids;
	private boolean ignorePassables;

	private final Queue<Vector> coordsHit;
	private Block nextBlock;

	/**
	 * @param start Start of line segment
	 * @param end End of line segment
	 * @param ignoreLiquids should liquids be ignored?
	 * @param ignorePassables should air and other passable blocks be ignored?
	 */
	public DDABlockIterator(World world, Vector start, Vector end, boolean ignoreLiquids, boolean ignorePassables) {
		this.world = world;
		setIgnoreLiquids(ignoreLiquids);
		setIgnorePassables(ignorePassables);
		//do DDA raycast and store results
		coordsHit = DDARaycasting.raycastWorldGrid(start, end);
		precalcNextBlock();
	}

	public void setIgnoreLiquids(boolean ignoreLiquids) {
		this.ignoreLiquids = ignoreLiquids;
	}

	public void setIgnorePassables(boolean ignorePassables) {
		this.ignorePassables = ignorePassables;
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
		//go through hit coords and find the first valid block
		while(!coordsHit.isEmpty()) {
			Vector pos = coordsHit.poll();
			Block b = world.getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());

			if(ignorePassables && b.isPassable())
				continue;
			if(ignoreLiquids && b.isLiquid())
				continue;

			nextBlock = b;
			break;
		}
	}
}
