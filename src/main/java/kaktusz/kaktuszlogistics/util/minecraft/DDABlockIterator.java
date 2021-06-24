package kaktusz.kaktuszlogistics.util.minecraft;

import kaktusz.kaktuszlogistics.util.DDARaycast;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Iterator;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.*;

/**
 * Iterates over all blocks which intersect a line segment using the DDA algorithm.
 */
public class DDABlockIterator implements Iterator<Block> {

	private final World world;
	private final boolean ignoreLiquids;
	private final boolean ignorePassables;
	private boolean allowChunkLoading = false;

	private final DDARaycast raycast;
	private Block nextBlock;
	private int chunkX = Integer.MIN_VALUE, chunkZ = Integer.MIN_VALUE; //the current chunk we are in

	/**
	 * @param start start of line segment
	 * @param end end of line segment
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

	/**
	 * @param allow if true, this block iterator will allow chunks to be loaded. Otherwise, it will terminate upon leaving the loaded area.
	 */
	public DDABlockIterator setAllowChunkLoading(boolean allow) {
		allowChunkLoading = allow;

		return this;
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

			if(!allowChunkLoading) {
				boolean chunkChanged = false; //check if we changed chunk, so that we don't need to call isChunkLoaded redundantly
				int chunk = blockToChunkCoord(pos.getBlockX());
				if(chunk != chunkX) {
					chunkX = chunk;
					chunkChanged = true;
				}
				chunk = blockToChunkCoord(pos.getBlockZ());
				if(chunk != chunkZ) {
					chunkZ = chunk;
					chunkChanged = true;
				}
				if (chunkChanged && !world.isChunkLoaded(chunkX, chunkZ)) //entered unloaded chunk
					return;
			}
			Block b = world.getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());

			if(ignorePassables && b.isPassable())
				continue;
			if(ignoreLiquids && b.isLiquid())
				continue;

			nextBlock = b;
		}
	}
}
