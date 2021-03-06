package kaktusz.kaktuszlogistics.world.multiblock;

import kaktusz.kaktuszlogistics.items.properties.multiblock.MultiblockTemplate;
import kaktusz.kaktuszlogistics.world.DurableBlock;
import kaktusz.kaktuszlogistics.world.KLChunk;
import kaktusz.kaktuszlogistics.world.KLWorld;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.*;
import static kaktusz.kaktuszlogistics.world.multiblock.components.DecoratorSpecialBlock.SpecialType;

/**
 * The core block of a multiblock structure
 */
public class MultiblockBlock extends DurableBlock {
	private static final long serialVersionUID = 100L;

	private transient MultiblockTemplate property;
	private BlockFace facing;
	private boolean structureValidCache = false;
	private BlockAABB aabbCache = null;
	protected final HashMap<SpecialType, Set<BlockPosition>> specialBlocksCache = new HashMap<>();

	public MultiblockBlock(MultiblockTemplate property, Location location, ItemMeta meta) {
		super(property, location, meta);
		this.property = property;
	}

	@Override
	protected void setUpTransients() {
		super.setUpTransients();
		property = (MultiblockTemplate)getType();
	}

	//BEHAVIOUR
	@Override
	public void onPlaced(BlockPlaceEvent e) {
		super.onPlaced(e);
		setFacingFromPlaceEvent(e);
		reverifyStructure();
	}

	public void setFacingFromPlaceEvent(BlockPlaceEvent e) {
		if(e.getBlockPlaced().getBlockData() instanceof FaceAttachable) {
			FaceAttachable.AttachedFace attachedFace = ((FaceAttachable)e.getBlockPlaced().getBlockData()).getAttachedFace();
			switch (attachedFace) {
				case FLOOR:
					setFacing(BlockFace.UP);
					return;
				case CEILING:
					setFacing(BlockFace.DOWN);
					return;
				default:
					break;
			}
		}
		if(e.getBlockPlaced().getBlockData() instanceof Directional) {
			setFacing(((Directional)e.getBlockPlaced().getBlockData()).getFacing());
			return;
		}

		BlockFace facing;
		Vector facingVector = e.getPlayer().getEyeLocation().getDirection().setY(0).multiply(-1);
		if(Math.abs(facingVector.getX()) > Math.abs(facingVector.getZ())) { //facing east/west
			facing = facingVector.getX() > 0 ? BlockFace.EAST : BlockFace.WEST;
		}
		else {
			facing = facingVector.getZ() > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
		}
		setFacing(facing);
	}

	@Override
	public void onRemoved(KLWorld world, int x, int y, int z) {
		registerWithChunks(false);
	}

	/**
	 * Updates multiblock data for all chunks which intersect this multiblock's AABB
	 * @param register registers if true, deregisters if false.
	 */
	private void registerWithChunks(boolean register) {
		KLWorld world = KLWorld.get(getLocation().getWorld());
		BlockAABB boundingBox = getAABB();
		BlockPosition pos = new BlockPosition(getLocation());
		int chunkMinX = blockToChunkCoord(boundingBox.minCorner.x);
		int chunkMinZ = blockToChunkCoord(boundingBox.minCorner.z);
		int chunkMaxX = blockToChunkCoord(boundingBox.maxCorner.x);
		int chunkMaxZ = blockToChunkCoord(boundingBox.maxCorner.z);
		for(int cz = chunkMinZ; cz <= chunkMaxZ; cz++) {
			for(int cx = chunkMinX; cx <= chunkMaxX; cx++) {
				KLChunk chunk = world.getChunkAt(cx, cz);
				if(chunk == null)
					continue;

				if(register) {
					Set<BlockPosition> multiblocks = chunk.getOrCreateExtraDataSet("multiblocks");
					multiblocks.add(pos);
				}
				else {
					chunk.removeFromExtraDataSet("multiblocks", pos);
				}
			}
		}
	}

	protected void onVerificationFailed() {

	}

	//GETTERS & SETTERS
	/**
	 * Get the direction that this multiblock is facing
	 */
	public BlockFace getFacing() {
		return facing;
	}

	/**
	 * Set the direction that this multiblock is facing
	 */
	public void setFacing(BlockFace facing) {
		this.facing = facing;

		//make block match
		Block block = getLocation().getBlock();
		BlockData blockData = block.getBlockData();
		if(blockData instanceof Directional) {
			if(!((Directional)blockData).getFaces().contains(facing)) //not a valid facing
				return;

			((Directional) blockData).setFacing(facing);
			block.setBlockData(blockData);
		}
	}

	//INFO
	/**
	 * Get the name of this multiblock
	 */
	public String getName() {
		return getProperty().getName();
	}

	/**
	 * @return Lore describing the details of this specific multiblock instance
	 */
	public List<String> getLore() {
		return null;
	}

	//STRUCTURE
	/**
	 * Re-verifies the structure and (de)registers it with the appropriate chunks
	 */
	public final boolean reverifyStructure() {
		//1. de-register from currently registered chunks and then clear the cache
		registerWithChunks(false);
		structureValidCache = false;
		aabbCache = null;
		specialBlocksCache.clear();
		//2. check if structure is valid
		boolean valid = getProperty().verifyStructure(this);
		if(valid) //3. if so, register with the chunks
			registerWithChunks(true);

		structureValidCache = valid;
		if(!valid)
			onVerificationFailed();
		return valid;
	}

	/**
	 * Checks if the multiblock structure is valid. Uses cache when possible.
	 */
	public boolean isStructureValid() {
		if(structureValidCache)
			return true;

		return structureValidCache = reverifyStructure();
	}

	/**
	 * Checks if the multiblock structure was valid last time reverifyStructure() was called.
	 * Unlike isStructureValid(), it will not try to reverify the structure if the cached validity is false.
	 */
	public boolean isStructureValid_cached() {
		return structureValidCache;
	}

	/**
	 * Gets the axis-aligned bounding box which tightly encompasses this structure
	 */
	public BlockAABB getAABB() {
		if(aabbCache != null)
			return aabbCache;

		return aabbCache = getProperty().getAABB(this);
	}

	/**
	 * Gets the cached AABB (may be null)
	 */
	protected BlockAABB getCachedAABB() {
		return aabbCache;
	}

	/**
	 * For multiblock property.
	 * Use this if the AABB is computed while verifying the structure, to avoid re-verifying just to get the AABB
	 */
	public void setAABBCache(BlockAABB newCacheValue) {
		aabbCache = newCacheValue;
	}

	/**
	 * Marks a block as being special in a given way. One block may be special in multiple different ways.
	 */
	public void markBlockSpecial(BlockPosition block, SpecialType specialType) {
		Set<BlockPosition> markedBlocks = specialBlocksCache.computeIfAbsent(specialType, k -> new HashSet<>());
		markedBlocks.add(block);
	}

	//HELPER
	public boolean isBlockPartOfMultiblock(Block block) {
		BlockPosition pos = new BlockPosition(block.getLocation());
		return isPosPartOfMultiblock(pos);
	}
	public boolean isPosPartOfMultiblock(BlockPosition position) {
		if(!isPosInAABB(position))
			return false;

		return getProperty().isPosPartOfMultiblock(position, this);
	}

	/**
	 * @param position The block position we want to check
	 */
	public boolean isPosInAABB(BlockPosition position) {
		return getAABB().containsPosition(position);
	}

	/**
	 * Gets the property which is responsible for handling the multiblock data
	 */
	public MultiblockTemplate getProperty() {
		return property;
	}
}
