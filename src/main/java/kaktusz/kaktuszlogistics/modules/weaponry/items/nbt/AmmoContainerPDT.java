package kaktusz.kaktuszlogistics.modules.weaponry.items.nbt;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

import java.util.StringJoiner;

public class AmmoContainerPDT implements PersistentDataType<String, AmmoContainerNBT> {

	public static final AmmoContainerPDT AMMO_CONTAINER_DATA = new AmmoContainerPDT();

	@Override
	public Class<String> getPrimitiveType() {
		return String.class;
	}

	@Override
	public Class<AmmoContainerNBT> getComplexType() {
		return AmmoContainerNBT.class;
	}

	@Override
	public String toPrimitive(AmmoContainerNBT ammoContainerNBT, PersistentDataAdapterContext persistentDataAdapterContext) {
		StringJoiner data = new StringJoiner(",");

		ammoContainerNBT.addFields(data);

		return data.toString();
	}

	@Override
	public AmmoContainerNBT fromPrimitive(String s, PersistentDataAdapterContext persistentDataAdapterContext) {
		return new AmmoContainerNBT(s.split(","));
	}
}
