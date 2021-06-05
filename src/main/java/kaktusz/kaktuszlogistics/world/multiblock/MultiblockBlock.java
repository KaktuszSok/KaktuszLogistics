package kaktusz.kaktuszlogistics.world.multiblock;

import kaktusz.kaktuszlogistics.items.properties.Multiblock;
import kaktusz.kaktuszlogistics.util.CastingUtils;
import kaktusz.kaktuszlogistics.world.DurableBlock;
import kaktusz.kaktuszlogistics.world.KLChunk;
import kaktusz.kaktuszlogistics.world.KLWorld;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashSet;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.*;

public class MultiblockBlock extends DurableBlock {

	private transient final Multiblock property;
	private boolean structureValidCache = false;
	private transient BlockAABB aabbCache = null;

	public MultiblockBlock(Multiblock property, ItemMeta meta) {
		super(property, meta);
		this.property = property;
	}

	//OVERRIDES
	@Override
	public ItemStack getDrop(Block block) {
		ItemStack drop = super.getDrop(block);
		ItemMeta meta = drop.getItemMeta();
		property.setFacing(meta, null);
		drop.setItemMeta(meta);
		return drop;
	}

	@Override
	public void onPlaced(BlockPlaceEvent e) {
		setFacingFromPlaceEvent(e);
		reverifyStructure(e.getBlockPlaced());
	}

	public void setFacingFromPlaceEvent(BlockPlaceEvent e) {
		BlockFace facing;
		Vector facingVector = e.getPlayer().getEyeLocation().getDirection().setY(0).multiply(-1);
		if(Math.abs(facingVector.getX()) > Math.abs(facingVector.getZ())) { //facing east/west
			facing = facingVector.getX() > 0 ? BlockFace.EAST : BlockFace.WEST;
		}
		else {
			facing = facingVector.getZ() > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
		}
		getProperty().setFacing(data, facing);

		//make placed block match
		BlockData blockData = e.getBlockPlaced().getBlockData();
		if(blockData instanceof Directional) {
			((Directional) blockData).setFacing(facing);
			e.getBlockPlaced().setBlockData(blockData);
		}
	}

	@Override
	public void onRemoved(KLWorld world, int x, int y, int z) {
		registerWithChunks(world, world.world.getBlockAt(x, y, z), false);
	}

	/**
	 * Updates multiblock data for all chunks which intersect this multiblock's AABB
	 * @param register registers if true, deregisters if false.
	 */
	private void registerWithChunks(KLWorld world, Block thisBlock, boolean register) {
		BlockAABB boundingBox = getAABB(thisBlock);
		int chunkMinX = blockToChunkCoord(boundingBox.minCorner.x);
		int chunkMinZ = blockToChunkCoord(boundingBox.minCorner.z);
		int chunkMaxX = blockToChunkCoord(boundingBox.maxCorner.x);
		int chunkMaxZ = blockToChunkCoord(boundingBox.maxCorner.z);
		for(int cz = chunkMinZ; cz <= chunkMaxZ; cz++) {
			for(int cx = chunkMinX; cx <= chunkMaxX; cx++) {
				KLChunk chunk = world.getOrCreateChunkAt(cx, cz);
				HashSet<BlockPosition> multiblocks = CastingUtils.confidentCast(chunk.getExtraData("multiblocks"));
				if(multiblocks == null)
					multiblocks = new HashSet<>();

				BlockPosition pos = new BlockPosition(thisBlock.getLocation());
				if(register)
					multiblocks.add(pos);
				else
					multiblocks.remove(pos);

				chunk.setExtraData("multiblocks", multiblocks);
				Bukkit.broadcastMessage("registerWithChunks - " + register + " - (" + cx + "," + cz + ")");
			}
		}
	}

	@Override
	public void onInteracted(PlayerInteractEvent e) {
		e.getPlayer().sendMessage("valid: " + isStructureValid(e.getClickedBlock()) + ", facing: " + getProperty().getFacing(data));
	}

	//STRUCTURE
	/**
	 * Re-verifies the structure and (de)registers it with the appropriate chunks
	 */
	public boolean reverifyStructure(Block thisBlock) {
		//1. de-register from currently registered chunks and then clear the cache
		KLWorld world = KLWorld.get(thisBlock.getWorld());
		registerWithChunks(world, thisBlock, false);
		structureValidCache = false;
		aabbCache = null;
		//2. check if structure is valid
		boolean valid = getProperty().verifyStructure(thisBlock, this);
		if(valid) //3. if so, register with the chunks
			registerWithChunks(world, thisBlock, true);

		structureValidCache = valid;
		return valid;
	}

	/**
	 * Checks if the multiblock structure is valid. Uses cache when possible.
	 */
	public boolean isStructureValid(Block thisBlock) {
		if(structureValidCache)
			return true;

		return structureValidCache = reverifyStructure(thisBlock);
	}

	public BlockAABB getAABB(Block thisBlock) {
		if(aabbCache != null)
			return aabbCache;

		return aabbCache = getProperty().getAABB(thisBlock, this);
	}

	public void setAABBCache(BlockAABB newCacheValue) {
		aabbCache = newCacheValue;
	}

	public boolean isBlockPartOfMultiblock(Block block, Block multiblock) {
		BlockPosition pos = new BlockPosition(block.getLocation());
		return isPosPartOfMultiblock(pos, multiblock);
	}
	public boolean isPosPartOfMultiblock(BlockPosition position, Block multiblock) {
		if(!isPosInAABB(position, multiblock))
			return false;

		return getProperty().isPosPartOfMultiblock(position, multiblock, this);
	}

	/**
	 * @param position The block position we want to check
	 * @param multiblock The physical block that corresponds to this multiblock
	 */
	public boolean isPosInAABB(BlockPosition position, Block multiblock) {
		return getAABB(multiblock).containsPosition(position);
	}

	//HELPER
	/**
	 * Gets the property which is responsible for handling the multiblock data
	 */
	public Multiblock getProperty() {
		return property;
	}
}
