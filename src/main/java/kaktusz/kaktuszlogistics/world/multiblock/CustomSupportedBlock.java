package kaktusz.kaktuszlogistics.world.multiblock;

import kaktusz.kaktuszlogistics.items.properties.multiblock.MultiblockTemplate;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A custom block which relies upon another block to support it (i.e. signs, banners, torches, etc)
 */
public abstract class CustomSupportedBlock extends MultiblockBlock {
	private static final long serialVersionUID = 100L;

	public CustomSupportedBlock(MultiblockTemplate property, Location location, ItemMeta meta) {
		super(property, location, meta);
	}

	/**
	 * @return The face of this block which faces the supporting block
	 */
	public abstract BlockFace getSupportedFace();

}
