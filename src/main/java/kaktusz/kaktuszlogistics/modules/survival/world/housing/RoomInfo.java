package kaktusz.kaktuszlogistics.modules.survival.world.housing;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.util.SetUtils;
import kaktusz.kaktuszlogistics.world.KLWorld;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Door;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.Future;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.*;

/**
 * Information about some enclosed area. Used to calculate labour supply etc.
 */
public class RoomInfo {
	/**
	 * Maximum extents of room, including walls. An enclosed area bigger than this is considered outside.
	 */
	public static int MAX_SIZE_HORIZONTAL = 33;
	/**
	 * Maximum extents of room, including floor and ceiling. An enclosed area bigger than this is considered outside.
	 */
	public static int MAX_SIZE_VERTICAL = 12;
	/**
	 * Maximum volume of room, including walls, floors and ceilings. An enclosed area bigger than this is considered outside.
	 */
	public static int MAX_VOLUME = MAX_SIZE_HORIZONTAL*MAX_SIZE_VERTICAL*MAX_SIZE_HORIZONTAL/3;

	private static final Set<Material> SOLID_FALSE_POSITIVES = SetUtils.setFromElements(
			Material.OAK_SIGN,
			Material.SPRUCE_SIGN,
			Material.BIRCH_SIGN,
			Material.JUNGLE_SIGN,
			Material.ACACIA_SIGN,
			Material.DARK_OAK_SIGN,
			Material.CRIMSON_SIGN,
			Material.WARPED_SIGN,

			Material.OAK_WALL_SIGN,
			Material.SPRUCE_WALL_SIGN,
			Material.BIRCH_WALL_SIGN,
			Material.JUNGLE_WALL_SIGN,
			Material.ACACIA_WALL_SIGN,
			Material.DARK_OAK_WALL_SIGN,
			Material.CRIMSON_WALL_SIGN,
			Material.WARPED_WALL_SIGN
	);
	private static final Set<Material> DOORS = SetUtils.setFromElements(
			Material.OAK_DOOR,
			Material.SPRUCE_DOOR,
			Material.BIRCH_DOOR,
			Material.JUNGLE_DOOR,
			Material.ACACIA_DOOR,
			Material.DARK_OAK_DOOR,
			Material.CRIMSON_DOOR,
			Material.WARPED_DOOR,
			Material.IRON_DOOR
	);

	/**
	 * Accessible floor area in blocks (square metres).
	 */
	private int floorArea = 0;
	private int beds = 0;
	private final List<BlockPosition> possibleConnectedRooms = new ArrayList<>();

	public int getFloorArea() {
		return floorArea;
	}

	public int getBeds() {
		return beds;
	}

	public List<BlockPosition> getPossibleConnectedRooms() {
		return possibleConnectedRooms;
	}

	/**
	 * @param world World to check for room in
	 * @param startPos Where to start the search from. Must be a block directly above the room's floor (i.e. adjacent to it from above)
	 * @param chunksCache A cache of chunk snapshots which will be populated as the algorithm finds new chunks
	 * @param accessibleBlocksCache A cache of blocks accessible in this room from the starting position. It will be populated by the algorithm.
	 * @return null if the room was too big or was not a room, otherwise the information about the room.
	 */
	public static RoomInfo calculateRoom(World world, BlockPosition startPos, Map<KLWorld.ChunkCoordinate, ChunkSnapshot> chunksCache, Set<BlockPosition> accessibleBlocksCache) {
		//1. check if we are in an enclosed area
		Set<BlockPosition> checkedBlocks = new HashSet<>();
		Stack<BlockPosition> blocksToCheck = new Stack<>();
		blocksToCheck.push(startPos);

		MutableBlockPosition minimumCorner = new MutableBlockPosition(startPos);
		MutableBlockPosition maximumCorner = new MutableBlockPosition(startPos);
		int volume = 0;
		while (!blocksToCheck.isEmpty()) {
			BlockPosition currBlock = blocksToCheck.pop();

			//update bounds and volume and check if we didn't violate the maximum values
			volume++;
			if(volume > MAX_VOLUME) //room too big
				return null;
			minimumCorner.x = Math.min(minimumCorner.x, currBlock.x);
			maximumCorner.x = Math.max(maximumCorner.x, currBlock.x);
			if(maximumCorner.x - minimumCorner.x > MAX_SIZE_HORIZONTAL) //room too big
				return null;
			minimumCorner.y = (short)Math.min(minimumCorner.y, currBlock.y);
			maximumCorner.y = (short)Math.max(maximumCorner.y, currBlock.y);
			if(maximumCorner.y - minimumCorner.y > MAX_SIZE_VERTICAL) //room too big
				return null;
			minimumCorner.z = Math.min(minimumCorner.z, currBlock.z);
			maximumCorner.z = Math.max(maximumCorner.z, currBlock.z);
			if(maximumCorner.z - minimumCorner.z > MAX_SIZE_HORIZONTAL) //room too big
				return null;

			if(isBlockSolid(world, currBlock, chunksCache))
				continue; //reached a wall - don't add its neighbours

			//mark block as checked
			checkedBlocks.add(currBlock);
			//push unchecked neighbours to stack
			pushManyToStack(blocksToCheck, checkedBlocks, currBlock.east(), currBlock.west(), currBlock.north(), currBlock.south(), currBlock.below(), currBlock.above());
		}

		//2. follow floor to calculate room's info (we only consider accessible regions)
		BlockPosition floorSearchStartBlock = startPos.below();
		if(!isBlockFloor(floorSearchStartBlock, getChunkSnapshotAtBlockPosition(world, floorSearchStartBlock, chunksCache)))
			return null; //failed to find floor

		RoomInfo info = new RoomInfo();
		blocksToCheck.add(startPos.below());
		while (!blocksToCheck.isEmpty()) {
			BlockPosition currBlock = blocksToCheck.pop();
			if(!accessibleBlocksCache.add(currBlock))
				continue; //this block was already checked
			ChunkSnapshot currChunk = getChunkSnapshotAtBlockPosition(world, currBlock, chunksCache);
			if(currChunk == null) //not good!
				continue;
			BlockPosition aboveBlock = currBlock.above();
			boolean checkedAboveBlock = accessibleBlocksCache.contains(aboveBlock);

			//special case: bed
			Bed.Part bedPart = null;
			if(!checkedAboveBlock) //if the above block hasn't been checked yet, see if we are under a bed
				bedPart = isBlockBedFloor(currBlock, currChunk);
			if(bedPart != null)
				accessibleBlocksCache.add(currBlock.above()); //add the bed, which is above the current block
			else
				bedPart = isBlockBed(currBlock, currChunk); //check if current block is the bed

			if(bedPart != null) {
				info.beds += bedPart.ordinal(); //0 if head, 1 if foot (this way we don't double-count beds)
				continue;
			}

			//special case: door
			if(DOORS.contains(getMaterialFromChunk(aboveBlock, currChunk))) {
				Door door = (Door)getBlockDataFromChunk(aboveBlock, currChunk);
				Vector doorDir = door.getFacing().getDirection();
				BlockPosition floorBehindDoor = new BlockPosition(currBlock.x + doorDir.getBlockX(), currBlock.y, currBlock.z + doorDir.getBlockZ());
				if(accessibleBlocksCache.contains(floorBehindDoor)) { //door was facing towards where we came from
					floorBehindDoor = new BlockPosition(currBlock.x - doorDir.getBlockX(), currBlock.y, currBlock.z - doorDir.getBlockZ());
				}
				//we might later discover the door leads to the same room, but we will adress that further down the line
				info.possibleConnectedRooms.add(floorBehindDoor.above());
				continue;
			}

			if(isBlockFloor(currBlock, currChunk)) { //case 1: floor
				info.floorArea++;
				pushManyToStack(blocksToCheck, accessibleBlocksCache, currBlock.east(), currBlock.west(), currBlock.north(), currBlock.south());
				continue;
			}
			//case 2,3,4: incline, decline or inaccessible
			if(!checkedAboveBlock && isBlockFloor(aboveBlock, currChunk)) { //case 2: incline
				info.floorArea++;
				pushManyToStack(blocksToCheck, accessibleBlocksCache, aboveBlock.east(), aboveBlock.west(), aboveBlock.north(), aboveBlock.south());
				accessibleBlocksCache.add(aboveBlock);
				continue;
			}
			BlockPosition belowBlock = currBlock.below();
			if(!accessibleBlocksCache.contains(belowBlock) && isBlockFloor(belowBlock, currChunk)) { //case 3: decline
				info.floorArea++;
				pushManyToStack(blocksToCheck, accessibleBlocksCache, belowBlock.east(), belowBlock.west(), belowBlock.north(), belowBlock.south());
				accessibleBlocksCache.add(belowBlock);
			}
			//case 4: inaccessible (do nothing - don't add neighbours)
		}
		//remove doors which lead to previously cached coordinates
		info.possibleConnectedRooms.removeIf(accessibleBlocksCache::contains);

		//don't count rooms with no space
		if(info.floorArea == 0)
			return null;

		return info;
	}

	private static boolean isBlockSolid(World world, BlockPosition position, Map<KLWorld.ChunkCoordinate, ChunkSnapshot> chunksCache) {
		ChunkSnapshot chunkSnapshot = getChunkSnapshotAtBlockPosition(world, position, chunksCache);
		if(chunkSnapshot == null) {
			return false;
		}

		return isBlockSolid(position, chunkSnapshot);
	}
	private static boolean isBlockSolid(BlockPosition position, ChunkSnapshot chunkSnapshot) {
		Material mat = getMaterialFromChunk(position, chunkSnapshot);
		return mat.isSolid() && !SOLID_FALSE_POSITIVES.contains(mat);
	}

	private static boolean isBlockFloor(BlockPosition position, ChunkSnapshot chunkSnapshot) {
		return isBlockSolid(position, chunkSnapshot)
				&& !isBlockSolid(position.above(), chunkSnapshot)
				&& !isBlockSolid(new BlockPosition(position.x, (short)(position.y+2), position.z), chunkSnapshot);
	}

	/**
	 * @return The appropriate bed part if the block is solid, the block above it a bed and the block above the bed is not solid. If the check fails, returns null.
	 */
	private static Bed.Part isBlockBedFloor(BlockPosition position, ChunkSnapshot chunkSnapshot) {
		if(!isBlockSolid(position, chunkSnapshot)) //not solid floor
			return null;

		return isBlockBed(position.above(), chunkSnapshot);
	}

	/**
	 * @return The appropriate bed part if the block is a bed and the block above the bed is not solid. If the check fails, returns null.
	 */
	private static Bed.Part isBlockBed(BlockPosition position, ChunkSnapshot chunkSnapshot) {
		BlockData data = getBlockDataFromChunk(position, chunkSnapshot);
		if(!(data instanceof Bed)) //block is not bed
			return null;

		//make sure the block above is not solid
		if(isBlockSolid(position.above(), chunkSnapshot))
			return null;

		return ((Bed)data).getPart();
	}

	private static ChunkSnapshot getChunkSnapshotAtBlockPosition(World world, BlockPosition position, Map<KLWorld.ChunkCoordinate, ChunkSnapshot> chunksCache) {
		KLWorld.ChunkCoordinate chunkCoord = new KLWorld.ChunkCoordinate(blockToChunkCoord(position.x), blockToChunkCoord(position.z));
		if(chunksCache.containsKey(chunkCoord)) {
			return chunksCache.get(chunkCoord);
		}
		else {
			//otherwise, we ventured into a new chunk
			ChunkSnapshot chunkSnapshot = getChunkSnapshot(world, chunkCoord);
			if(chunkSnapshot == null) //oh shit, something went wrong!
				return null;
			chunksCache.put(chunkCoord, chunkSnapshot);
			return chunkSnapshot;
		}
	}

	private static ChunkSnapshot getChunkSnapshot(World world, KLWorld.ChunkCoordinate where) {
		if(Bukkit.isPrimaryThread()) {
			return world.getChunkAt(where.chunkX, where.chunkZ).getChunkSnapshot();
		}

		Future<ChunkSnapshot> future = Bukkit.getScheduler().callSyncMethod(KaktuszLogistics.INSTANCE,
				() -> world.getChunkAt(where.chunkX, where.chunkZ).getChunkSnapshot());

		try {
			return future.get();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param position Global position of block
	 */
	private static Material getMaterialFromChunk(BlockPosition position, ChunkSnapshot chunkSnapshot) {
		return chunkSnapshot.getBlockData(blockToLocalCoord(position.x), position.y, blockToLocalCoord(position.z))
				.getMaterial();
	}
	/**
	 * @param position Global position of block
	 */
	private static BlockData getBlockDataFromChunk(BlockPosition position, ChunkSnapshot chunkSnapshot) {
		return chunkSnapshot.getBlockData(blockToLocalCoord(position.x), position.y, blockToLocalCoord(position.z));
	}

	/**
	 * Pushes many elements to the stack, ignoring them if they are in a certain set
	 */
	@SafeVarargs
	private static <T> void pushManyToStack(Stack<T> stack, Set<T> ignore, T... elements) {
		for(T element : elements) {
			if(!ignore.contains(element))
				stack.push(element);
		}
	}
}
