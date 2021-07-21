package kaktusz.kaktuszlogistics.world;

import kaktusz.kaktuszlogistics.items.properties.ItemPlaceable;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class CustomSignBlock extends CustomBlock {
	private static final long serialVersionUID = 100L;

	private transient Sign signCache;

	public CustomSignBlock(ItemPlaceable prop, Location location, ItemMeta meta) {
		super(prop, location, meta);
	}

	@Override
	public ItemStack getDrop(Block block) {
		return new ItemStack(block.getDrops().iterator().next()); //drop sign
	}

	/**
	 * Gets the state of the sign
	 * @return The state of the sign or null if something is wrong
	 */
	protected Sign getState() {
		if(signCache != null)
			return signCache;

		BlockState data = getLocation().getBlock().getState();
		if(!(data instanceof Sign) || !(data.getBlockData() instanceof WallSign))
			return null;

		return signCache = (Sign)data;
	}

	/**
	 * Gets the block data of the sign
	 * @return The block data of the sign or null if something is wrong
	 */
	protected WallSign getWallSign() {
		Sign state = getState();
		if(state == null)
			return null;

		return (WallSign)state.getBlockData();
	}

}
