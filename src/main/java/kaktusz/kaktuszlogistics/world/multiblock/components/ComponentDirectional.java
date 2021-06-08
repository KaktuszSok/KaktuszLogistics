package kaktusz.kaktuszlogistics.world.multiblock.components;

import kaktusz.kaktuszlogistics.items.properties.Multiblock;
import kaktusz.kaktuszlogistics.world.multiblock.MultiblockBlock;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

public class ComponentDirectional extends MultiblockComponent {

	private final Multiblock.RELATIVE_DIRECTION direction;
	private boolean allowOpposite = false;

	public ComponentDirectional(Multiblock.RELATIVE_DIRECTION direction) {
		this.direction = direction;
	}

	public ComponentDirectional setAllowOpposite(boolean allowOpposite) {
		this.allowOpposite = allowOpposite;

		return this;
	}

	@Override
	public boolean match(Block block, MultiblockBlock multiblock) {
		if(block.getBlockData() instanceof Directional) {
			BlockFace multiblockFacing = multiblock.getFacing();
			BlockFace blockFacing = ((Directional)block.getBlockData()).getFacing();
			if(!allowOpposite)
				return Multiblock.relativeDirectionFromGlobal(multiblockFacing, blockFacing) == direction;
			else
				return Multiblock.relativeDirectionFromGlobal(multiblockFacing, blockFacing) == direction
				|| Multiblock.relativeDirectionFromGlobal(multiblockFacing, blockFacing.getOppositeFace()) == direction;
		}

		return false;
	}
}
