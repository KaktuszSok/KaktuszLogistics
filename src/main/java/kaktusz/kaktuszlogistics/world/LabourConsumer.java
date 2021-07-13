package kaktusz.kaktuszlogistics.world;

import org.apache.commons.lang.mutable.MutableDouble;
import org.bukkit.Location;

import java.util.*;
import java.util.function.Function;

import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.BlockPosition;
import static kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils.blockToChunkCoord;

@SuppressWarnings({"UnnecessaryInterfaceModifier", "SameReturnValue"}) //readability
public interface LabourConsumer {

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

		BlockPosition selfPos = new BlockPosition(getLocation());
		MutableDouble amountLeft = new MutableDouble(amount);
		return ChunkSupplySystem.requestFromNearbyChunks(getLocation(), "labourSuppliers",
				(Function<LabourSupplier, Boolean>) supplier -> {
					//test for labour availability
					if(supplier.getLabourTier() < tierRequired)
						return false;

					double labourAvailable = supplier.getLabourPerDay() - supplier.getTotalLabourSupplied();
					if(labourAvailable <= 0)
						return false;

					//register with supplier
					double labourUsed = Math.min(amountLeft.doubleValue(), labourAvailable);
					amountLeft.setValue(amountLeft.doubleValue() - labourUsed);
					suppliersToAdd.put(supplier, labourUsed);

					if(amountLeft.doubleValue() <= 0) { //supply total meets or exceeds demand - register with suppliers
						for (Map.Entry<LabourSupplier, Double> entry : suppliersToAdd.entrySet()) {
							entry.getKey().getLabourConsumers().put(selfPos, entry.getValue()); //register self with supplier
							getLabourSuppliers().add(new BlockPosition(entry.getKey().getLocation())); //register supplier with self
						}
						return true; //done!
					}
					return false;
				});
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

		if(amountSupplied == getRequiredLabour()) {
			onValidateSupplyFinished(true);
			return true; //all is fine
		}

		boolean success = requestLabour(); //otherwise, fetch labour again
		onValidateSupplyFinished(success);
		return success;
	}

	default void onValidateSupplyFinished(boolean requirementsMet) {

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
