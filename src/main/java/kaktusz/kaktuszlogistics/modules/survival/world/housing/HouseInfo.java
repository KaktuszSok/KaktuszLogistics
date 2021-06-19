package kaktusz.kaktuszlogistics.modules.survival.world.housing;

import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import kaktusz.kaktuszlogistics.world.KLWorld;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;

import java.util.*;

public class HouseInfo {

	/**
	 * A house which caches more chunks than this amount will be considered too large
	 */
	public static final int CHUNKS_CACHE_MAX = 25;

	public final List<RoomInfo> rooms = new ArrayList<>();

	/**
	 * @return Null if we are not in a house (or house is too big), otherwise the HouseInfo that was calculated
	 */
	public static HouseInfo calculateHouse(World world, VanillaUtils.BlockPosition startPos) {
		Map<KLWorld.ChunkCoordinate, ChunkSnapshot> chunksCache = new HashMap<>();
		Set<VanillaUtils.BlockPosition> accessibleBlocksCache = new HashSet<>();
		Queue<VanillaUtils.BlockPosition> roomCandidates = new LinkedList<>();
		roomCandidates.add(startPos);

		HouseInfo house = new HouseInfo();
		while (!roomCandidates.isEmpty()) {
			if(chunksCache.size() > CHUNKS_CACHE_MAX)
				return null; //house too big

			VanillaUtils.BlockPosition roomStartPoint = roomCandidates.poll();
			RoomInfo foundRoom = RoomInfo.calculateRoom(world, roomStartPoint, chunksCache, accessibleBlocksCache);
			accessibleBlocksCache.add(roomStartPoint);
			if(foundRoom == null)
				continue;

			house.rooms.add(foundRoom);
			roomCandidates.addAll(foundRoom.getPossibleConnectedRooms());
		}

		return house.rooms.size() == 0 ? null : house;
	}

	public int getTotalFloorArea() {
		return rooms.stream().mapToInt(RoomInfo::getFloorArea).sum();
	}

	public int getTotalBeds() {
		return rooms.stream().mapToInt(RoomInfo::getBeds).sum();
	}

	public int getTier() {
		int pop = getMaxPopulation();
		if(pop == 0)
			return 0;
		int areaPerPerson = getTotalFloorArea()/pop;
		double num = 6.0d;
		int log = 0;
		while (num <= areaPerPerson) {
			num *= 1.4d;
			log++;
		}
		return log;
	}

	public int getMaxPopulation() {
		return Math.min(getTotalBeds(), getTotalFloorArea() / 6);
	}

	@Override
	public String toString() {
		return "HouseInfo{" +
				"rooms=" + rooms.toString() +
				'}';
	}
}
