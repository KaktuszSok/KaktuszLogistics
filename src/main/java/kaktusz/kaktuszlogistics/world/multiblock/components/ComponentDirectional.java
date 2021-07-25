package kaktusz.kaktuszlogistics.world.multiblock.components;

import kaktusz.kaktuszlogistics.items.properties.multiblock.MultiblockTemplate;
import kaktusz.kaktuszlogistics.world.multiblock.MultiblockBlock;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

public class ComponentDirectional extends MultiblockComponent {

	private final MultiblockTemplate.RELATIVE_DIRECTION direction;
	private boolean allowOpposite = false;

	public ComponentDirectional(MultiblockTemplate.RELATIVE_DIRECTION direction) {
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
				return MultiblockTemplate.relativeDirectionFromGlobal(multiblockFacing, blockFacing) == direction;
			else
				return MultiblockTemplate.relativeDirectionFromGlobal(multiblockFacing, blockFacing) == direction
				|| MultiblockTemplate.relativeDirectionFromGlobal(multiblockFacing, blockFacing.getOppositeFace()) == direction;
		}

		return false;
	}
}
