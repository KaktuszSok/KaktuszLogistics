package kaktusz.kaktuszlogistics.items.nbt;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

import java.util.StringJoiner;

public class MultiblockDataPDT implements PersistentDataType<String, MultiblockDataNBT> {

	public static final MultiblockDataPDT MULTIBLOCK_DATA = new MultiblockDataPDT();

	@Override
	public Class<String> getPrimitiveType() {
		return String.class;
	}

	@Override
	public Class<MultiblockDataNBT> getComplexType() {
		return MultiblockDataNBT.class;
	}

	@Override
	public String toPrimitive(MultiblockDataNBT multiblockDataNBT, PersistentDataAdapterContext persistentDataAdapterContext) {
		StringJoiner data = new StringJoiner(",");

		multiblockDataNBT.addFields(data);

		return data.toString();
	}

	@Override
	public MultiblockDataNBT fromPrimitive(String s, PersistentDataAdapterContext persistentDataAdapterContext) {
		return new MultiblockDataNBT(s.split(","));
	}
}
