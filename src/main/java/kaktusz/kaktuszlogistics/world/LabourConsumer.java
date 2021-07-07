package kaktusz.kaktuszlogistics.world;

import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import org.bukkit.Location;

import java.util.*;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.BlockPosition;
import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.blockToChunkCoord;

@SuppressWarnings("UnnecessaryInterfaceModifier") //readability
public interface LabourConsumer {
	static final int SEARCH_RADIUS = 3;

	Set<BlockPosition> getLabourSuppliers();
	Location getLocation();

	/**
	 * @return The amount of labour per day this consumer requires
	 */
	double getRequiredLabour();

	/**
	 * @return The tier of labour this consumer requires
	 */
	int getTier();

	/**
	 * Tries to find nearby labour suppliers and, if the total labour supply is sufficient, registers with them.
	 * Calling this will deregister self from all current suppliers.
	 * @return True if enough labour was available to fulfill the request, false if not.
	 */
	default boolean requestLabour() {
		return requestLabour(getRequiredLabour());
	}
	/**
	 * Tries to find nearby labour suppliers and, if the total labour supply is sufficient, registers with them.
	 * Calling this will deregister self from all current suppliers.
	 * @return True if enough labour was available to fulfill the request, false if not.
	 */
	default boolean requestLabour(double amount) {
		deregisterFromAllSuppliers();
		if(amount == 0)
			return true;

		Map<LabourSupplier, Double> suppliersToAdd = new HashMap<>();
		int tierRequired = getTier();

		Location location = getLocation();
		KLWorld world = KLWorld.get(location.getWorld());
		int chunkOffsetX = blockToChunkCoord(location.getBlockX());
		int chunkOffsetZ = blockToChunkCoord(location.getBlockZ());
		int searchDist = 0; //how far from the centre (in chunks) are we searching
		while (amount > 0 && searchDist <= SEARCH_RADIUS) {
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
					Set<BlockPosition> suppliersInChunk = currChunk.getExtraData("labourSuppliers");
					if(suppliersInChunk == null)
						continue;
					suppliersInChunk = new HashSet<>(suppliersInChunk);

					for(BlockPosition supplierPos : suppliersInChunk) {
						CustomBlock block = currChunk.getBlockAt(supplierPos.x, supplierPos.y, supplierPos.z);
						if(!(block instanceof LabourSupplier)) { //bad data
							currChunk.removeFromExtraDataSet("labourSuppliers", supplierPos); //fix data
							continue;
						}
						LabourSupplier supplier = (LabourSupplier) block;

						//test for labour availability
						if(supplier.getLabourTier() < tierRequired)
							continue;

						double labourAvailable = supplier.getLabourPerDay() - supplier.getTotalLabourSupplied();
						if(labourAvailable <= 0)
							continue;

						//register with supplier
						double labourUsed = Math.min(amount, labourAvailable);
						amount -= labourUsed;
						suppliersToAdd.put(supplier, labourUsed);

						if(amount <= 0) { //supply total meets or exceeds demand - register with suppliers
							for (Map.Entry<LabourSupplier, Double> entry : suppliersToAdd.entrySet()) {
								entry.getKey().getLabourConsumers().put(new BlockPosition(location), entry.getValue()); //register self with supplier
								getLabourSuppliers().add(new BlockPosition(entry.getKey().getLocation())); //register supplier with self
							}
							return true;
						}
					}
				}
			}
			searchDist++;
		}
		return false;
	}

	/**
	 * Removes any invalid suppliers and fetches new suppliers if the amount of labour supplied does not equal the amount of labour needed
	 * @return True if the supply meets the demand after the validation and fixes, if any, are applied
	 */
	default boolean validateAndFixSupply() {
		int amountSupplied = 0;
		int tierRequired = getTier();
		Set<BlockPosition> suppliers = new HashSet<>(getLabourSuppliers());
		KLWorld world = KLWorld.get(getLocation().getWorld());
		BlockPosition selfPos = new BlockPosition(getLocation());
		for (BlockPosition supplierPos : suppliers) {
			CustomBlock block = world.getBlockAt(supplierPos.x, supplierPos.y, supplierPos.z);
			if(!(block instanceof LabourSupplier)) { //bad data
				KLChunk chunk = world.getChunkAt(blockToChunkCoord(supplierPos.x), blockToChunkCoord(supplierPos.z));
				if(chunk != null) {
					chunk.removeFromExtraDataSet("labourSuppliers", supplierPos); //fix data
					getLabourSuppliers().remove(supplierPos); //deregister supplier from self
				}
				continue;
			}
			LabourSupplier supplier = (LabourSupplier) block;
			Double amountFromSupplier = supplier.getLabourConsumers().get(selfPos);
			if(amountFromSupplier == null) { //bad data - supplier does not supply us
				getLabourSuppliers().remove(supplierPos); //deregister supplier from self
				continue;
			}
			if(supplier.getLabourTier() < tierRequired) { //tier wrong
				supplier.getLabourConsumers().remove(selfPos); //deregister self from supplier
				getLabourSuppliers().remove(supplierPos); //deregister supplier from self
				continue;
			}
			amountSupplied += amountFromSupplier;
		}

		if(amountSupplied == getRequiredLabour())
			return true; //all is fine

		return requestLabour(); //otherwise, fetch labour again
	}

	/**
	 * Removes self from all suppliers and removes all suppliers from self
	 */
	default void deregisterFromAllSuppliers() {
		Set<BlockPosition> suppliers = getLabourSuppliers();
		KLWorld world = KLWorld.get(getLocation().getWorld());
		BlockPosition selfPos = new BlockPosition(getLocation());
		for (BlockPosition supplierPos : suppliers) {
			CustomBlock block = world.getBlockAt(supplierPos.x, supplierPos.y, supplierPos.z);
			if(!(block instanceof LabourSupplier)) { //bad data
				KLChunk chunk = world.getChunkAt(blockToChunkCoord(supplierPos.x), blockToChunkCoord(supplierPos.z));
				if(chunk != null) {
					chunk.removeFromExtraDataSet("labourSuppliers", supplierPos); //fix data
				}
				continue;
			}
			LabourSupplier supplier = (LabourSupplier) block;
			supplier.getLabourConsumers().remove(selfPos); //de-register self from supplier
		}
		suppliers.clear(); //de-register suppliers from self
	}
}
