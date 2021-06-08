package kaktusz.kaktuszlogistics.world.multiblock;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.properties.Multiblock;
import kaktusz.kaktuszlogistics.util.CastingUtils;
import kaktusz.kaktuszlogistics.world.DurableBlock;
import kaktusz.kaktuszlogistics.world.KLChunk;
import kaktusz.kaktuszlogistics.world.KLWorld;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.*;
import static kaktusz.kaktuszlogistics.world.multiblock.components.DecoratorSpecialBlock.SpecialType;

/**
 * The core block of a multiblock structure
 */
public class MultiblockBlock extends DurableBlock {

	public static NamespacedKey FACING_KEY;

	private transient final Multiblock property;
	private boolean structureValidCache = false;
	private transient BlockAABB aabbCache = null;
	protected transient final HashMap<SpecialType, Set<BlockPosition>> specialBlocksCache = new HashMap<>();

	public MultiblockBlock(Multiblock property, Location location, ItemMeta meta) {
		super(property, location, meta);
		this.property = property;
	}

	//BEHAVIOUR
	@Override
	public ItemStack getDrop(Block block) {
		ItemStack drop = super.getDrop(block);
		ItemMeta meta = drop.getItemMeta();
		//noinspection ConstantConditions
		meta.getPersistentDataContainer().remove(FACING_KEY);
		drop.setItemMeta(meta);
		return drop;
	}

	@Override
	public void onPlaced(BlockPlaceEvent e) {
		setFacingFromPlaceEvent(e);
		reverifyStructure();
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
		setFacing(facing);

		//make placed block match
		BlockData blockData = e.getBlockPlaced().getBlockData();
		if(blockData instanceof Directional) {
			((Directional) blockData).setFacing(facing);
			e.getBlockPlaced().setBlockData(blockData);
		}
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
		KLWorld world = KLWorld.get(location.getWorld());
		BlockAABB boundingBox = getAABB();
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

				BlockPosition pos = new BlockPosition(location);
				if(register)
					multiblocks.add(pos);
				else
					multiblocks.remove(pos);

				chunk.setExtraData("multiblocks", multiblocks);
			}
		}
	}

	@Override
	public void onInteracted(PlayerInteractEvent e) {

	}

	protected void onVerificationFailed() {

	}

	//INFO
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
	public boolean reverifyStructure() {
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

	public BlockAABB getAABB() {
		if(aabbCache != null)
			return aabbCache;

		return aabbCache = getProperty().getAABB(this);
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

	//NBT
	public BlockFace getFacing() {
		Byte facing = CustomItem.readNBT(data, FACING_KEY, PersistentDataType.BYTE);
		if(facing == null) {
			setFacing(BlockFace.NORTH);
			facing = 0;
		}
		return BlockFace.values()[facing];
	}
	public void setFacing(BlockFace facing) {
		if(facing == null || facing.getModY() != 0) //don't allow null values or facing up/down
			facing = BlockFace.NORTH; //default value
		CustomItem.setNBT(data, FACING_KEY, PersistentDataType.BYTE, (byte)facing.ordinal());
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

	//HELPER
	/**
	 * Gets the property which is responsible for handling the multiblock data
	 */
	public Multiblock getProperty() {
		return property;
	}
}
