package kaktusz.kaktuszlogistics.world;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.BlockPosition;
import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.blockToChunkCoord;

public interface LabourSupplier {
	Map<BlockPosition, Double> getLabourConsumers();
	Location getLocation();

	/**
	 * @return The amount of labour per day this supplier can supply
	 */
	double getLabourPerDay();

	/**
	 * @return The tier of labour this supplier can supply
	 */
	int getLabourTier();

	//REGISTRATION
	/**
	 * Call this when the supplier becomes available to the world
	 */
	default void registerAsLabourSupplier() {
		Location location = getLocation();
		KLWorld world = KLWorld.get(location.getWorld());
		KLChunk chunk = world.getOrCreateChunkAt(blockToChunkCoord(location.getBlockX()), blockToChunkCoord(location.getBlockZ()));
		BlockPosition selfPos = new BlockPosition(location);
		registerAsLabourSupplier(chunk, selfPos);
	}
	/**
	 * Call this when the supplier becomes available to the world
	 */
	default void registerAsLabourSupplier(KLChunk chunk, BlockPosition selfPos) {
		chunk.getOrCreateExtraDataSet("labourSuppliers").add(selfPos); //register with chunk
	}

	/**
	 * Call this to remove the supplier from the world
	 */
	default void deregisterAsLabourSupplier() {
		Location location = getLocation();
		KLWorld world = KLWorld.get(location.getWorld());
		KLChunk chunk = world.getOrCreateChunkAt(blockToChunkCoord(location.getBlockX()), blockToChunkCoord(location.getBlockZ()));
		BlockPosition selfPos = new BlockPosition(location);
		deregisterAsLabourSupplier(chunk, selfPos);
	}
	/**
	 * Call this to remove the supplier from the world
	 */
	default void deregisterAsLabourSupplier(KLChunk chunk, BlockPosition selfPos) {
		chunk.removeFromExtraDataSet("labourSuppliers", selfPos); //deregister from chunk
		//remove all consumers
		Map<BlockPosition, Double> consumers = getLabourConsumers();
		Set<LabourConsumer> consumersToFix = new HashSet<>();
		for(BlockPosition consumerPos : consumers.keySet()) {
			CustomBlock block = chunk.world.getBlockAt(consumerPos.x, consumerPos.y, consumerPos.z);
			if(!(block instanceof LabourConsumer))
				continue;
			LabourConsumer consumer = (LabourConsumer) block;
			consumer.getLabourSuppliers().remove(selfPos);
			consumersToFix.add(consumer);
		}
		consumers.clear();
		Bukkit.getScheduler().runTaskLater(KaktuszLogistics.INSTANCE,
				() -> consumersToFix.forEach(LabourConsumer::validateAndFixSupply), 1);
	}

	default double getTotalLabourSupplied() {
		double result = 0;
		for (double amount : getLabourConsumers().values()) {
			result += amount;
		}
		return result;
	}
}
