package kaktusz.kaktuszlogistics.items.nbt;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.CustomItemManager;
import kaktusz.kaktuszlogistics.items.properties.ammo.AmmoContainer;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.StringJoiner;

public class AmmoContainerNBT {

	public final CustomItem containerItem;
	public int ammoCount;

	//SETUP
	public AmmoContainerNBT(CustomItem item) {
		this.containerItem = item;
	}
	public AmmoContainerNBT(AmmoContainer base) {
		this.containerItem = base.item;
		this.ammoCount = base.getMaxAmmo();
	}
	public AmmoContainerNBT(String[] fields) {
		this.containerItem = CustomItemManager.tryGetItem(fields[0].split("=")[1]);
		if(!readFields(fields)) {
			KaktuszLogistics.LOGGER.warning("Could not read fields of AmmoContainerNBT. Fields: " + Arrays.toString(fields));
		}
	}

	//READ-WRITE
	protected void addFields(StringJoiner joiner) {
		joiner.add("ItemType=" + containerItem.type);
		joiner.add("AmmoCount=" + ammoCount);
	}

	/**
	 * Reads in fields (except for field 0, ItemType) from serialised string
	 * @return True if successful
	 */
	private boolean readFields(String[] fields) {
		if(fields.length < 2)
			return false;
		String ammoStr = fields[1].split("=")[1];
		ammoCount = Integer.parseInt(ammoStr);

		return true;
	}

	//HELPER
	/**
	 * Tries to reduce the ammunition in the container by one
	 * @return True if the container was not empty and ammo could be consumed successfully
	 */
	public boolean consumeAmmo() {
		if(ammoCount <= 0)
			return false;
		ammoCount--;
		return true;
	}

	public AmmoContainer getAmmoContainerProperty() {
		return containerItem.findProperty(AmmoContainer.class);
	}

	public ItemStack createStack() {
		return getAmmoContainerProperty().createStackFromData(this);
	}
}
