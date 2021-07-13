package kaktusz.kaktuszlogistics.world;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.BlockPosition;
import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.blockToChunkCoord;

public class ChunkSupplySystem {
	static final int SEARCH_RADIUS = 3;

	/**
	 *
	 * @param location The location from which we will search outwards chunk by chunk, until a limit
	 * @param setName The name of the extra data set that contains the suppliers' BlockPositions
	 * @param consumeFunction A function which accepts some supplier of type T and runs consumption logic.
	 *                        It returns true if consumption has been satisfied.
	 *                        It returns false otherwise, indicating that our search should continue.
	 * @param <T> The type of supplier that the aforementioned extra data set should contain
	 * @return True if consumption has been satisfied. False if there were not enough supplies nearby.
	 */
	@SuppressWarnings("unchecked")
	public static <T> boolean requestFromNearbyChunks(Location location, String setName, Function<T, Boolean> consumeFunction) {
		KLWorld world = KLWorld.get(location.getWorld());
		int chunkOffsetX = blockToChunkCoord(location.getBlockX());
		int chunkOffsetZ = blockToChunkCoord(location.getBlockZ());
		int searchDist = 0; //how far from the centre (in chunks) are we searching
		while (searchDist <= SEARCH_RADIUS) {
			//search along borders starting from middle, then alternating left/right outwards until searchDist
			for(int delta = 0; delta == 0 || delta < searchDist; delta = (delta >= 0) ? -(delta+1) : -delta) {
				//search each border
				int dx,dz;
				for(int border = 0; border < 4; border++) {
					if(searchDist == 0 && border > 0)
						break;
					switch (border) {
						case 0: //north
							dx = delta;
							dz = -searchDist;
							break;
						case 1: //east
							dx = searchDist;
							dz = delta;
							break;
						case 2: //south
							dx = delta;
							dz = searchDist;
							break;
						case 3: //west
							dx = -searchDist;
							dz = delta;
							break;
						default:
							dx = dz = 0;
							break;
					}

					KLChunk currChunk = world.getChunkAt(chunkOffsetX + dx, chunkOffsetZ + dz);
					if(currChunk == null)
						continue;
					Set<BlockPosition> suppliersInChunk = currChunk.getExtraData(setName);
					if(suppliersInChunk == null)
						continue;
					suppliersInChunk = new HashSet<>(suppliersInChunk); //clone set, as original may get modified

					for(BlockPosition supplierPos : suppliersInChunk) {
						CustomBlock block = currChunk.getBlockAt(supplierPos.x, supplierPos.y, supplierPos.z);
						if(block == null) { //bad data
							currChunk.removeFromExtraDataSet(setName, supplierPos); //fix data
							continue;
						}

						T supplier;
						//can't do instanceof with generics so instead use a try-catch
						try {
							supplier = (T)block;
						} catch (ClassCastException e) { //bad data
							currChunk.removeFromExtraDataSet(setName, supplierPos); //fix data
							continue;
						}

						//execute consume function
						if(consumeFunction.apply(supplier))
							return true;
					}
				}
			}
			searchDist++;
		}
		return false;
	}
}
