package kaktusz.kaktuszlogistics.items.nbt;

import kaktusz.kaktuszlogistics.KaktuszLogistics;
import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.CustomItemManager;
import kaktusz.kaktuszlogistics.items.properties.Multiblock;
import org.bukkit.block.BlockFace;

import java.util.Arrays;
import java.util.StringJoiner;

public class MultiblockDataNBT {
	public final CustomItem multiblockItem;
	public BlockFace facing = BlockFace.NORTH;

	//SETUP
	public MultiblockDataNBT(CustomItem item) {
		this.multiblockItem = item;
	}
	public MultiblockDataNBT(String[] fields) {
		this.multiblockItem = CustomItemManager.tryGetItem(fields[0].split("=")[1]);
		if(!readFields(fields)) {
			KaktuszLogistics.LOGGER.warning("Could not read fields of MultiblockDataNBT. Fields: " + Arrays.toString(fields));
		}
	}

	//READ-WRITE
	protected void addFields(StringJoiner joiner) {
		joiner.add("ItemType=" + multiblockItem.type);
		joiner.add("Facing=" + facing.ordinal());
	}

	/**
	 * Reads in fields (except for field 0, ItemType) from serialised string
	 * @return True if successful
	 */
	private boolean readFields(String[] fields) {
		if(fields.length < 2)
			return false;
		facing = BlockFace.values()[Integer.parseInt(fields[1].split("=")[1])];

		return true;
	}

	//HELPER
	public Multiblock getMultiblockProperty() {
		return multiblockItem.findProperty(Multiblock.class);
	}
}
